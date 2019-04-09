
import org.platanios.tensorflow.api._
import org.platanios.tensorflow.api.learn._
import org.platanios.tensorflow.api.learn.layers._
import org.platanios.tensorflow.api.learn.estimators.InMemoryEstimator
import org.platanios.tensorflow.api.core.client.FeedMap
import org.tensorflow.framework.GraphDef

import org.platanios.tensorflow.api.ops.Files
import org.platanios.tensorflow.api.ops.Image

import java.net.URL
import sys.process._
import java.io.{BufferedInputStream, File, FileInputStream}

import object_detection.protos.string_int_label_map.{StringIntLabelMap,StringIntLabelMapItem}

import _root_.io.circe.{Decoder, Encoder}

case class Detection(`class`: String, score: Float, box: (Int, Int, Int, Int))

object Detection {
  import _root_.io.circe.generic.semiauto._

  implicit lazy val encoder: Encoder[Detection] = deriveEncoder[Detection]
  implicit lazy val decoder: Decoder[Detection] = deriveDecoder[Detection]
}

class ObjectDetectService() {

  val modelHub = new ObjectDetectHub()

  if (!modelHub.isModelLocal()) modelHub.getModel()
  if (!modelHub.isModelMapLocal()) modelHub.getModelMap()

/*
// bootstrap by downloading model
  val cacheDir = sys.env("HOME") + "/data/models/tmp"
  val modelName = "ssd_mobilenet_v2_coco_2018_03_29"
  val archiveFilename = s"${modelName}.tar.gz"
  val frozenGraphFilename = "frozen_inference_graph.pb"

  val modelURL = s"http://download.tensorflow.org/models/object_detection/${archiveFilename}"


  //new URL(modelURL) #> new File(s"${cacheDir}/${archiveFilename}") !!
  //s"tar -xzf ${cacheDir}/${archiveFilename} -C ${cacheDir}" !!

// bootstrap by downloading class map
  val mapFilename = "mscoco_label_map.pbtxt"
  val mapUrl = s"https://raw.githubusercontent.com/tensorflow/models/master/research/object_detection/data/${mapFilename}"
  //new URL(mapUrl) #> new File(s"${cacheDir}/${modelName}/${mapFilename}") !

  */

  val mapFile = modelHub.modelMapPath()

  val labelMap: scala.collection.Map[Int, String] = {
      val pbText = scala.io.Source.fromFile(mapFile).mkString
      val stringIntLabelMap = StringIntLabelMap.fromAscii(pbText)
      stringIntLabelMap.item.collect {
        case StringIntLabelMapItem(_, Some(id), Some(displayName)) =>
          id -> displayName
      }.toMap
    }

  val modelGraphPath = modelHub.modelPath() //s"${cacheDir}/${modelName}/${frozenGraphFilename}"
  lazy val graphDef = GraphDef.parseFrom(new BufferedInputStream(new FileInputStream(new File(modelGraphPath))))
  val graph = Graph.fromGraphDef(graphDef)
  val session = Session(graph)

  val imagePlaceholder = graph.getOutputByName("image_tensor:0").toUByte

  val detectionBoxes = graph.getOutputByName("detection_boxes:0")
  val detectionScores = graph.getOutputByName("detection_scores:0")
  val detectionClasses = graph.getOutputByName("detection_classes:0")
  val numDetections = graph.getOutputByName("num_detections:0")

  val (imgTensor, fileNamePlaceholder) = tf.createWith(graph = graph) {
    val fileNamePlaceholder = tf.placeholder[String]()
    val fileTensor = Files.readFile(fileNamePlaceholder)
    val imgTensor = Image.decodePng(fileTensor, 3)
    (imgTensor, fileNamePlaceholder)
  }

  def detect(file: File) = {

    // Feed the image file to get the Images Tensor
    val fileNameTensor = Tensor.fill(Shape())(file.getAbsolutePath())
    val feedImg = FeedMap(fileNamePlaceholder, fileNameTensor)
    val imageOuts: Tensor[UByte] =
      session.run(fetches = imgTensor, feeds = feedImg)

    // Retain image sizes to format output later
    val width = imageOuts.shape(1)
    val height = imageOuts.shape(0)

    // Feed with Images to compute detections:
    val feeds = FeedMap(imagePlaceholder, imageOuts.slice(NewAxis, ---))
    val Seq(boxes, scores, classes, num) = session.run(fetches = Seq(detectionBoxes, detectionScores, detectionClasses, numDetections), feeds = feeds)

  val labelList =
      for {
        i <- 0 until num(0).scalar.asInstanceOf[Float].toInt
        labelId = classes(0, i).toFloat.scalar.toInt
        //label = labelMap.getOrElse(labelId, "unknown")
        //if setOfClasses.isEmpty || setOfClasses.contains(label)

        box = boxes(0, i).toFloat.entriesIterator.toSeq
        x1 = (box(1) * width).toInt
        y1 = (box(0) * height).toInt
        x2 = (box(3) * width).toInt
        y2 = (box(2) * height).toInt
        labelBox = (x1, y1, x2 - x1 + 1, y2 - y1 + 1)
        score = scores(0, i).toFloat.scalar
      } yield (labelId, score, labelBox)
    labelList.toSeq
  }

  def labelize(detections: Seq[(Int, Float, (Int, Int, Int, Int))]) = detections.map {
      d => d match {
          case (id, score, box) if (labelMap.contains(id)) => Detection(labelMap(id), score, box)
          case (id, score, box) => Detection("unknown", score, box)
      }
  }

  def detectAndLabel(file: File) = labelize(detect(file))

}
