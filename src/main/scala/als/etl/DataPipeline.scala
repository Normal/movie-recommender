package als.etl

import als.common.EtlParams
import als.etl.stage.DataLoader
import org.apache.spark.sql.{DataFrame, SparkSession}

object DataPipeline {

  def run(params: EtlParams)(implicit spark: SparkSession): DataFrame = {

    val loader = new DataLoader(params.data.header, params.data.delimiter)

    val ratings = loader.loadRatings(params.data.ratingsFilePath)
    val movies = loader.loadMovies(params.data.moviesFilePath)

    ratings.join(movies, Seq("movie_id"), "inner")
  }
}
