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

case class Constraints(maxUserEvents: Int)

case class EtlParams(
                      data: InputDataParams,
                      filter: List[String],
                      constraints: Constraints
                    )

object ConfigLoader {

  val logger: Logger = Logger[this.type]

  def loadEtl(args: Array[String]): EtlParams = {
    val config: Config = createConfig(args)
    logger.info(s"App configuration: ${config.getConfig("als.etl")}")

    val inputData = InputDataParams(
      ratingsFilePath = config.getString("als.etl.input.ratings"),
      moviesFilePath = config.getString("als.etl.input.movies"),
      delimiter = config.getString("als.etl.input.delimiter"),
      header = config.getBoolean("als.etl.input.header")
    )

    EtlParams(
      filter = config.getStringList("als.etl.filter").toList,
      data = inputData,
      constraints = Constraints(
        maxUserEvents = config.getInt("als.etl.constraint.max_user_events")
      )
    )
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
