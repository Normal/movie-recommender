package als.etl.stage

import als.common.Constraints
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SparkSession}


class ConstraintsFilter(params: Constraints)(implicit spark: SparkSession) {

  def filter(df: DataFrame): DataFrame = {
    import org.apache.spark.sql.functions._

    val w = Window.partitionBy("user_id").orderBy(desc("timestamp"))

    df
      .withColumn("event_no", row_number().over(w))
      .filter(col("event_no") < params.maxUserEvents)
      .drop("event_no")
  }

}
