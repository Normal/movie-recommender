package als

import als.common.{ConfigLoader, EtlParams}
import als.etl.DataPipeline
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object TrainLauncher {

  def main(args: Array[String]): Unit = {
    val appConf: EtlParams = ConfigLoader.loadEtl(args)

    val sparkConf = new SparkConf()
      .setAppName("ALS movielens example")
      .setMaster("local[*]")

    implicit val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    val df = DataPipeline.run(appConf)

    df.show()
  }

}
