package als

import als.calc.{Id, CalculationEngine}
import als.common.{AppParams, ConfigLoader}
import als.etl.DataPipeline
import als.preparator.ModelPreparator
import als.train.AlsTraining
import org.apache.spark.SparkConf
import org.apache.spark.ml.recommendation.ALSModel
import org.apache.spark.sql.SparkSession

import scala.util.Random

object AppLauncher {

  def main(args: Array[String]): Unit = {
    val appConf: AppParams = ConfigLoader.loadConfig(args)

    val sparkConf = new SparkConf()
      .setAppName("ALS movielens example")
      .setMaster("local[*]")

    implicit val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    val (df, users, items) = DataPipeline.run(appConf.etl)

    val alg = new AlsTraining(appConf.training)
    val model: ALSModel = alg.trainModel(df)

    val scorer: CalculationEngine = ModelPreparator.prepare(
      model, items, users, appConf.training.rank
    )

    val randomUsers: Seq[Id] = Random.shuffle(users.map(_._1)).take(5)
    val randomItems: Seq[Id] = Random.shuffle(items.map(_._1)).take(5)

    println()
    println()
    randomUsers.map(scorer.recommendationsForUser(_, 10)).map(_.mkString(", ")).foreach(println)
    randomItems.map(scorer.recommendationsForItem(_, 10)).map(_.mkString(", ")).foreach(println)
    println()
    println()
  }

}
