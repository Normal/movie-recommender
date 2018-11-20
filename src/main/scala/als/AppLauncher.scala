package als

import als.calc.{CalculationEngine, RecommendationService}
import als.common.{AppParams, ConfigLoader}
import als.etl.DataPipeline
import als.preparator.ModelPreparator
import als.train.AlsTraining
import als.web.WebServer
import org.apache.spark.SparkConf
import org.apache.spark.ml.recommendation.ALSModel
import org.apache.spark.sql.SparkSession

object AppLauncher {

  def main(args: Array[String]): Unit = {
    val appConf: AppParams = ConfigLoader.loadConfig(args)

    val sparkConf = new SparkConf()
      .setAppName("ALS movielens example")
      .setMaster("local[*]")

    implicit val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    val (df, users, items, moviesData) = DataPipeline.run(appConf.etl)

    val alg = new AlsTraining(appConf.training)
    val model: ALSModel = alg.trainModel(df)

    val scorer: CalculationEngine = ModelPreparator.prepare(
      model, items, users, appConf.training.rank
    )

    val service = new RecommendationService(scorer, items.map(_._1), users.map(_._1))

    WebServer.start(service, moviesData)
  }

}
