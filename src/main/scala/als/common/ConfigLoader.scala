package als.common

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import grizzled.slf4j.Logger
import scala.collection.JavaConversions._

case class InputDataParams(
                            ratingsFilePath: String,
                            moviesFilePath: String,
                            delimiter: String,
                            header: Boolean
                          )

case class Constraints(minMovieRatings: Int, minUserRatings: Int)

case class EtlParams(
                      data: InputDataParams,
                      filter: List[String],
                      constraints: Constraints
                    )

case class TrainingParams(
                           seed: Int,
                           rank: Int,
                           regParam: Double,
                           alpha: Double,
                           maxIter: Int,
                           numUserBlocks: Int,
                           numItemBlocks: Int
                         )

case class AppParams(
                      etl: EtlParams,
                      training: TrainingParams
                    )

object ConfigLoader {

  val logger: Logger = Logger[this.type]

  def loadConfig(args: Array[String]): AppParams = {
    val config: Config = createConfig(args)
    logger.info(s"App configuration: ${config.getConfig("als.etl")}")

    val inputData = InputDataParams(
      ratingsFilePath = config.getString("als.etl.input.ratings"),
      moviesFilePath = config.getString("als.etl.input.movies"),
      delimiter = config.getString("als.etl.input.delimiter"),
      header = config.getBoolean("als.etl.input.header")
    )

    val etlParams = EtlParams(
      filter = config.getStringList("als.etl.filter").toList,
      data = inputData,
      constraints = Constraints(
        minMovieRatings = config.getInt("als.etl.constraint.min_movie_ratings"),
        minUserRatings = config.getInt("als.etl.constraint.min_user_ratings")
      )
    )

    val trainingParams = TrainingParams(
      seed = config.getInt("als.train.alg.seed"),
      rank = config.getInt("als.train.alg.rank"),
      regParam = config.getDouble("als.train.alg.reg_param"),
      alpha = config.getDouble("als.train.alg.alpha"),
      maxIter = config.getInt("als.train.alg.max_iter"),
      numUserBlocks = config.getInt("als.train.alg.num_user_blocks"),
      numItemBlocks = config.getInt("als.train.alg.num_item_blocks")
    )

    AppParams(etlParams, trainingParams)
  }

  private def createConfig(args: Array[String]): Config = {
    val config = if (!args.isEmpty) {
      val myConfigFile = new File(args.head)
      val fileConfig = ConfigFactory.parseFile(myConfigFile)
      ConfigFactory.load(fileConfig)
    } else ConfigFactory.load
    config
  }

}
