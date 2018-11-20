package als.calc

import breeze.linalg.{DenseMatrix, DenseVector, argtopk}


case class ItemScore(item: Id, score: Float)


// Important! All user related information can be moved into some key/value store instead.
@SerialVersionUID(1L)
class CalculationEngine(
                  itemMatrix: DenseMatrix[Float],
                  index2Item: Map[Index, Id],
                  item2Index: Map[Id, Index],
                  userMatrix: DenseMatrix[Float],
                  index2User: Map[Index, Id],
                  user2Index: Map[Id, Index]
                ) {

  def recommendationsForUser(userId: Id, num: Int): Seq[ItemScore] = {
    val userVector = getVector(user2Index(userId), userMatrix)
    val scores = topK(userVector, num)
    scores
  }

  def recommendationsForItem(itemId: Id, num: Int): Seq[ItemScore] = {
    val itemVector = getVector(item2Index(itemId), itemMatrix)
    val scores = topK(itemVector, num + 1)
    // remove item itself from recommendations
    scores.filter(_.item != itemId).take(num)
  }

  private def getVector(index: Index, matrix: DenseMatrix[Float]): DenseVector[Float] =
    matrix(index, ::).t

  private def scoresVector(vector: DenseVector[Float]): DenseVector[Float] =
    itemMatrix * vector

  private def topK(vector: DenseVector[Float], k: Int): Seq[ItemScore] = {
    val scores = scoresVector(vector)

    argtopk(scores, k).
      map(idx => ItemScore(index2Item(idx), scores(idx))).
      sortBy(-_.score)
  }
}


object CalculationEngine {

  def apply(
             rank: Int,
             itemFactors: TraversableOnce[(Id, Seq[Float])],
             itemMapping: TraversableOnce[(Id, Index)],
             userFactors: TraversableOnce[(Id, Seq[Float])],
             userMapping: TraversableOnce[(Id, Index)]
           ): CalculationEngine = {

    val index2Item = itemMapping.map(x => (x._2, x._1)).toMap
    val item2Index = itemMapping.map(x => (x._1, x._2)).toMap
    val itemMatrix = buildMatrix(rank, index2Item.keys.max, itemFactors, item2Index)

    val index2User = userMapping.map(x => (x._2, x._1)).toMap
    val user2Index = userMapping.map(x => (x._1, x._2)).toMap
    val userMatrix = buildMatrix(rank, index2User.keys.max, userFactors, user2Index)

    new CalculationEngine(itemMatrix, index2Item, item2Index, userMatrix, index2User, user2Index)
  }

  private[als] def buildMatrix(
                                rank: Int,
                                maxIndex: Int,
                                itemFactors: TraversableOnce[(Index, Seq[Float])],
                                itemToIndex: Map[Id, Index]
                              ): DenseMatrix[Float] = {
    val features = DenseMatrix.zeros[Float](maxIndex + 1, rank)
    for ((row, fac) <- itemFactors) {
      // TODO: nicer/faster way of row assignment?
      for ((x, col) <- fac.zipWithIndex) {
        features(itemToIndex(row), col) = x
      }
    }
    features
  }
}
