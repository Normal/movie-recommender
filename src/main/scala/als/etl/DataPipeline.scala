package als.etl

import als.calc.{Id, Index}
import als.common.EtlParams
import als.etl.stage.{ConstraintsApplier, DataLoader}
import grizzled.slf4j.Logger
import org.apache.spark.sql.{DataFrame, SparkSession}

object DataPipeline {

  val logger: Logger = Logger[this.type]

  type Users = Seq[(Id, Index)]
  type Movies = Seq[(Id, Index)]

  def run(params: EtlParams)(implicit spark: SparkSession): (DataFrame, Users, Movies) = {

    val loader = new DataLoader(params.data.header, params.data.delimiter)
    val constrains = new ConstraintsApplier(params.constraints)

    val ratings = loader.loadRatings(params.data.ratingsFilePath)
    val movies = loader.loadMovies(params.data.moviesFilePath)

    val df = ratings.join(movies, Seq("movie_id"), "inner")

    val filtered = constrains.filter(df)

    val usersDistinct = filtered.select("user_id").distinct().collect().map(_.getInt(0))
    val itemsDistinct = filtered.select("movie_id").distinct().collect().map(_.getInt(0))

    (filtered, usersDistinct.zipWithIndex, itemsDistinct.zipWithIndex)
  }
}
