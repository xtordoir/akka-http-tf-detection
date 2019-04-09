import org.slf4j.{Logger, LoggerFactory}
import akka.actor.ActorSystem
import akka.stream.{IOResult, Materializer}
import akka.stream.scaladsl.{FileIO, Source}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.ByteString
import akka.http.scaladsl.model.StatusCodes

import scala.util.{Failure, Success}
import java.nio.file.{Files, Paths}
import java.io.File

object Main  {
  private val log: Logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]) {

    implicit val system = ActorSystem("main-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

    val detectService = new ObjectDetectService()

    val route =
      path("") {
        get {
          complete(StatusCodes.OK)
        }
      } ~
      path("detect") {
          post {
              fileUpload("img") {
                  case (metadata, byteSource) =>
                  val file = File.createTempFile("image", ".png")
                  val fileSaveFut = byteSource.runWith(FileIO.toPath(Paths.get(file.getAbsolutePath)))
                  onComplete(fileSaveFut) {
                      case Success(s) =>
                          val detections = detectService.detectAndLabel(file)
                          complete(detections)
                      case Failure(s) => complete(s.getMessage)
                  }
              }
          }
      }

      val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
      bindingFuture.onComplete {
        case Success(binding) =>
          log.info("Success! Bound on {}", binding.localAddress)
        case Failure(error) => log.error("Failed", error)
        case _              => system.terminate()
      }
      scala.io.StdIn.readLine()
      bindingFuture.flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate()) // and shutdown when done

  }
}
