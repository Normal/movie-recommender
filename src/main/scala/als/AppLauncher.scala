package als

import als.common.{AppParams, ConfigLoader}
import als.etl.DataPipeline
import als.train.AlsTraining
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object AppLauncher {

  def main(args: Array[String]): Unit = {
    val appConf: AppParams = ConfigLoader.loadConfig(args)

    val sparkConf = new SparkConf()
      .setAppName("ALS movielens example")
      .setMaster("local[*]")

    implicit val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    val df = DataPipeline.run(appConf.etl)

    val alg = new AlsTraining(appConf.training)
    val model = alg.trainModel(df)

    model.itemFactors.show(truncate = false)
    model.userFactors.show(truncate = false)
  }

}
