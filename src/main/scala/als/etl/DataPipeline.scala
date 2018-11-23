package als.etl

import als.calc.{Id, Index}
import als.EtlParams
import als.etl.stage.{ConstraintsApplier, DataLoader}
import grizzled.slf4j.Logger
import org.apache.spark.sql.{DataFrame, SparkSession}
import scala.collection.JavaConversions._


object DataPipeline {

  val logger: Logger = Logger[this.type]

  type Users = Seq[(Id, Index)]
  type Movies = Seq[(Id, Index)]

  def run(params: EtlParams)(implicit spark: SparkSession):
  (DataFrame, Users, Movies, Map[Int, String], Map[Id, (List[String], List[Int])]) = {

    val loader = new DataLoader(params.data.header, params.data.delimiter)
    val constrains = new ConstraintsApplier(params.constraints)

    val ratings = loader.loadRatings(params.data.ratingsFilePath)
    val movies = loader.loadMovies(params.data.moviesFilePath)

    val df = ratings.join(movies, Seq("movie_id"), "inner")

    val filtered = constrains.filter(df)

    val usersDistinct = filtered.select("user_id").distinct().collect().map(_.getInt(0))
    val itemsDistinct = filtered.select("movie_id").distinct().collect().map(_.getInt(0))

    val moviesData = filtered.select("movie_id", "title").distinct()
      .collect().map(row => row.getInt(0) -> row.getString(1)).toMap

    import org.apache.spark.sql.functions._
    val userHistory: Map[Id, (List[String], List[Int])] = filtered.select("user_id", "title", "movie_id")
      .groupBy("user_id")
      .agg(collect_set("title").as("titles"), collect_set("movie_id").as("ids"))
      .collect().map(r => r.getInt(0) -> (r.getList[String](1).toList, r.getList[Int](2).toList)).toMap

    (filtered, usersDistinct.zipWithIndex, itemsDistinct.zipWithIndex, moviesData, userHistory)
  }
}
