package als

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import als.calc.{Id, ItemScore, RecommendationService}
import grizzled.slf4j.Logger
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


case class PredictionResponse(
                               id: Id,
                               scores: Seq[ScoreInfo],
                               movie: Option[String] = None,
                               history: Seq[MovieRating] = Seq.empty
                             )

case class MovieRating(title: String, movieId: Int, rating: Float)

case class ScoreInfo(id: Id, title: String, score: Float)

object WebServer {

  private val logger: Logger = Logger[this.type]

  def start(service: RecommendationService, movies: Map[Int, String]): Unit = {


    implicit val system: ActorSystem = ActorSystem("als-web")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    //    implicit val queryFormat: RootJsonFormat[Query] = jsonFormat4(Query)
    implicit val scoreFormat: RootJsonFormat[ScoreInfo] = jsonFormat3(ScoreInfo)
    implicit val movieRatingFormat: RootJsonFormat[MovieRating] = jsonFormat3(MovieRating)
    implicit val resultFormat: RootJsonFormat[PredictionResponse] = jsonFormat4(PredictionResponse)

    def scoresToData(scores: Seq[ItemScore]): Seq[ScoreInfo] =
      scores.map(i => ScoreInfo(i.item, movies(i.item), i.score))

    def getRecsForUser(id: Id, scores: Seq[ItemScore]) = {
      val userHistory = service.getUserHistory(id).filter(_.rating >= 4.5)
      val results: PredictionResponse = PredictionResponse(id, scoresToData(scores), history = userHistory)
      complete(results)
    }

    val route = {
      get {
        pathPrefix("user" / "random") {
          val (id, scores) = service.forRandomUser
          getRecsForUser(id, scores)
        }
      } ~
      get {
        path("user" / IntNumber) { id =>
          val scores = service.forUser(id)
          getRecsForUser(id, scores)
        }
      } ~
      get {
        pathPrefix("item" / "random") {
          val (id, scores) = service.forRandomItem
          val results: PredictionResponse = PredictionResponse(id, scoresToData(scores), movie = Some(movies(id)))
          complete(results)
        }
      } ~
      get {
        path("item" / IntNumber) { id =>
          val scores = service.forItem(id)
          val results: PredictionResponse = PredictionResponse(id, scoresToData(scores), movie = Some(movies(id)))
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
