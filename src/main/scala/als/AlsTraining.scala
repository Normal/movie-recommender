package als

import grizzled.slf4j.Logger
import org.apache.spark.ml.recommendation.{ALS, ALSModel}
import org.apache.spark.sql.DataFrame

class AlsTraining(params: TrainingParams) {

  val logger: Logger = Logger[this.type]

  def trainModel(df: DataFrame): ALSModel = {

    val als = new ALS().
      setUserCol("user_id").
      setItemCol("movie_id").
      setRatingCol("rating").
      setPredictionCol("score").
      setImplicitPrefs(true).
      setSeed(params.seed).
      setRank(params.rank).
      setRegParam(params.regParam).
      setAlpha(params.alpha).
      setMaxIter(params.maxIter).
      setNumUserBlocks(params.numUserBlocks).
      setNumItemBlocks(params.numItemBlocks)

    logger.info("Training the model...")
    val model = als.fit(df)
    logger.debug("Training is complete!")
    model
  }
}
