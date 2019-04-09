import com.typesafe.config.ConfigFactory
import java.io.File
import java.net.URL
import sys.process._

class ObjectDetectHub() {
  private val config = ConfigFactory.load()

  private val modelConfig = config.getConfig("model")

  val cacheDir = modelConfig.getString("base")
  val modelName = modelConfig.getString("name")
  val archiveFilename = s"${modelName}.tar.gz"

  val frozenGraphFilename = modelConfig.getString("frozenGraphFilename")
  val modelURL = modelConfig.getString("Url")

  val mapFilename = modelConfig.getString("mapFilename")
  val mapUrl = modelConfig.getString("mapUrl")

  def isModelLocal() = {
    println(s"${cacheDir}/${modelName}/${frozenGraphFilename}")
    (new File(s"${cacheDir}/${modelName}/${frozenGraphFilename}")).exists()
  }

  def getModel() = {
    new URL(modelURL) #> new File(s"${cacheDir}/${archiveFilename}") !!
    val cmd = s"tar -xzf ${cacheDir}/${archiveFilename} -C ${cacheDir}"
    cmd.!!;
    s"${cacheDir}/${modelName}/${frozenGraphFilename}"
  }

  def modelPath() = s"${cacheDir}/${modelName}/${frozenGraphFilename}"

  def isModelMapLocal() = {
    println(s"${cacheDir}/${modelName}/${mapFilename}")
    (new File(s"${cacheDir}/${modelName}/${mapFilename}")).exists()
  }

  def getModelMap() = {
    new URL(mapUrl) #> new File(s"${cacheDir}/${modelName}/${mapFilename}") !!

    s"${cacheDir}/${modelName}/${mapFilename}"
  }

  def modelMapPath() = s"${cacheDir}/${modelName}/${mapFilename}"

}
