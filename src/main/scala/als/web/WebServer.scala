package als.web

import java.time.{LocalDateTime, ZoneId}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import als.calc.{ItemScore, RecommendationService}
import grizzled.slf4j.Logger
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._


case class PredictionResponse(
                               id: Int,
                               scores: Seq[ItemScore]
                             )

object WebServer {

  private val logger: Logger = Logger[this.type]

  def start(service: RecommendationService): Unit = {


    implicit val system: ActorSystem = ActorSystem("als-web")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    //    implicit val queryFormat: RootJsonFormat[Query] = jsonFormat4(Query)
    implicit val scoreFormat: RootJsonFormat[ItemScore] = jsonFormat2(ItemScore)
    implicit val resultFormat: RootJsonFormat[PredictionResponse] = jsonFormat2(PredictionResponse)

    val route = {
      get {
        pathPrefix("user" / "random") {
          val (id, scores) = service.forRandomUser
          val results: PredictionResponse = PredictionResponse(id, scores)
          complete(results)
        }
      } ~
      get {
        pathPrefix("item" / "random") {
          val (id, scores) = service.forRandomItem
          val results: PredictionResponse = PredictionResponse(id, scores)
          complete(results)
        }
      }
    }


    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    logger.info(s"Started localhost:8080 ...")

    scala.sys.addShutdownHook {
      logger.info("Terminating...")
      bindingFuture
        .flatMap(_.unbind())
        .onComplete { _ =>
          materializer.shutdown()
          system.terminate()
        }
      Await.result(system.whenTerminated, 60 seconds)
      logger.info("Terminated... Bye")
    }

  }
}
