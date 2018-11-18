package als.preparator

import als.calc.{Id, Index, CalculationEngine}
import org.apache.spark.ml.recommendation.ALSModel

object ModelPreparator {

  def prepare(
               model: ALSModel,
               items: Seq[(Id, Index)],
               users: Seq[(Id, Index)],
               rank: Int
             ): CalculationEngine = {

    val userFactors = model.userFactors.rdd
      .map(r => (r.getInt(0), r.getSeq[Float](1)))
      .collect()

    val itemFactors = model.itemFactors.rdd
      .map(r => (r.getInt(0), r.getSeq[Float](1)))
      .collect()

    CalculationEngine(
      rank = rank,
      itemFactors = itemFactors,
      itemMapping = items,
      userFactors = userFactors,
      userMapping = users
    )
  }

}
