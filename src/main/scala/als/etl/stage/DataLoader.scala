package als.etl.stage

import grizzled.slf4j.Logger
import org.apache.spark.sql.functions.{col, split}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SparkSession}

class DataLoader(header: Boolean, delimiter: String)(implicit spark: SparkSession) {

  val logger: Logger = Logger[this.type]

  def loadRatings(path: String): DataFrame = {
    val scheme: StructType = StructType(Array(
      StructField("user_id", IntegerType, nullable = false),
      StructField("movie_id", IntegerType, nullable = false),
      StructField("rating", DoubleType, nullable = false),
      StructField("timestamp", LongType, nullable = false)
    ))

    load(path, scheme)
  }

  def loadMovies(path: String): DataFrame = {
    val scheme = StructType(Array(
      StructField("movie_id", IntegerType, nullable = false),
      StructField("title", StringType, nullable = false),
      StructField("genres_str", StringType, nullable = false)
    ))

    load(path, scheme)
      .withColumn("genres", split(col("genres_str"), "\\|"))
      .drop("genres_str")
  }

  private def load(path: String, scheme: StructType): DataFrame = {
    logger.info(s"Load df for source file $path")

    spark.read.format("csv")
      .options(Map("header" -> header.toString, "delimiter" -> delimiter, "quote" -> "\""))
      .schema(scheme)
      .load(path)
  }
}
