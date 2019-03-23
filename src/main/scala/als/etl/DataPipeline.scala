package als.etl

import java.util

import als.{EtlParams, MovieRating}
import als.calc.{Id, Index}
import als.etl.stage.DataLoader
import grizzled.slf4j.Logger
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable


object DataPipeline {

  val logger: Logger = Logger[this.type]

  type Users = Seq[(Id, Index)]
  type Movies = Seq[(Id, Index)]

  def run(params: EtlParams)(implicit spark: SparkSession):
  (DataFrame, Users, Movies, Map[Int, String], Map[Id, Seq[MovieRating]]) = {

    val loader = new DataLoader(params.data.header, params.data.delimiter)
//    val constrains = new ConstraintsApplier(params.constraints)

    val ratings = loader.loadRatings(params.data.ratingsFilePath)
    val movies = loader.loadMovies(params.data.moviesFilePath)

    val df = ratings.join(movies, Seq("movie_id"), "inner")

    val filtered = df; //constrains.filter(df)

    val usersDistinct = filtered.select("user_id").distinct().collect().map(_.getInt(0))
    val itemsDistinct = filtered.select("movie_id").distinct().collect().map(_.getInt(0))

    val moviesData = filtered.select("movie_id", "title").distinct()
      .collect().map(row => row.getInt(0) -> row.getString(1)).toMap

    import org.apache.spark.sql.functions._
    import scala.collection.JavaConverters._
    val userHistory: Map[Id, mutable.Buffer[MovieRating]] = filtered
      .select("user_id", "title", "movie_id", "rating")
      .select(col("user_id"), concat(col("title"), lit("%%"), col("movie_id"), lit("%%"), col("rating")).as("movie_rating"))
      .groupBy("user_id")
      .agg(collect_list("movie_rating"))
      .collect()
      .map(row => row.getInt(0) -> row.getList[String](1).asScala)
      .map(t2 => (t2._1, t2._2.map(Util(_))))
      .toMap

    (filtered, usersDistinct.zipWithIndex, itemsDistinct.zipWithIndex, moviesData, userHistory)
  }
}

object Util {

  def apply(str: String): MovieRating = {
    val parts = str.split("%%")
    MovieRating(parts(0), parts(1).toInt, parts(2).toFloat)
  }
}
