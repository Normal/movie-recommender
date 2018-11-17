package als.etl

import als.common.EtlParams
import als.etl.stage.{ConstraintsApplier, DataLoader}
import grizzled.slf4j.Logger
import org.apache.spark.sql.{DataFrame, SparkSession}

object DataPipeline {

  val logger: Logger = Logger[this.type]

  def run(params: EtlParams)(implicit spark: SparkSession): DataFrame = {

    val loader = new DataLoader(params.data.header, params.data.delimiter)
    val constrains = new ConstraintsApplier(params.constraints)

    val ratings = loader.loadRatings(params.data.ratingsFilePath)
    val movies = loader.loadMovies(params.data.moviesFilePath)

    val df = ratings.join(movies, Seq("movie_id"), "inner")

    val filtered = constrains.filter(df)

    filtered
  }
}
