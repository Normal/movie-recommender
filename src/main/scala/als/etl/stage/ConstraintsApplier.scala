package als.etl.stage

import als.Constraints
import org.apache.spark.sql.{DataFrame, SparkSession}


class ConstraintsApplier(params: Constraints)(implicit spark: SparkSession) {

  def filter(df: DataFrame): DataFrame = {
    import org.apache.spark.sql.functions._

    def grouping: (DataFrame, String) => DataFrame = (df1: DataFrame, col1: String) =>
      df1
        .groupBy(col1)
        .agg(count(col("rating")).as("count"))
        .select(col1, "count")

    df.cache()

    val allowedUsers = grouping(df, "user_id")
      .filter(col("count") > params.minUserRatings)
      .select("user_id")

    val allowedMovies = grouping(df, "movie_id")
      .filter(col("count") > params.minMovieRatings)
      .select("movie_id")


    df
      .join(allowedUsers, Seq("user_id"))
      .join(allowedMovies, Seq("movie_id"))
  }

}
