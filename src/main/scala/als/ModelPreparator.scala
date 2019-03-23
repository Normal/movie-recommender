package als

import als.calc.{Scorer, Id, Index}
import org.apache.spark.ml.recommendation.ALSModel

object ModelPreparator {

  def prepare(
               model: ALSModel,
               items: Seq[(Id, Index)],
               users: Seq[(Id, Index)],
               rank: Int
             ): Scorer = {

    val userFactors = model.userFactors.rdd
      .map(r => (r.getInt(0), r.getSeq[Float](1)))
      .collect()

    val itemFactors = model.itemFactors.rdd
      .map(r => (r.getInt(0), r.getSeq[Float](1)))
      .collect()

    Scorer(
      rank = rank,
      itemFactors = itemFactors,
      itemMapping = items,
      userFactors = userFactors,
      userMapping = users
    )
  }

}
