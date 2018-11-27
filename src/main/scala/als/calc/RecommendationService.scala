package als.calc

import scala.util.Random

class RecommendationService(
                             scorer: CalculationEngine,
                             items: Seq[Id],
                             users: Seq[Id],
                             userHistory: Map[Id, (List[String], List[Int])]
                           ) {

  val rand = new Random()

  def forUser(userId: Id): Seq[ItemScore] = {
    val userHistoryIds = userHistory.get(userId).map(_._2).getOrElse(List.empty)
    scorer.recommendationsForUser(userId, 10, userHistoryIds)
  }

  def forItem(itemId: Id): Seq[ItemScore] =
    scorer.recommendationsForItem(itemId, 10)

  def forRandomUser: (Id, Seq[ItemScore]) = {
    val randomIndex = rand.nextInt(users.length)
    val randomUser = users(randomIndex)

    (randomUser, forUser(randomUser))
  }

  def forRandomItem: (Id, Seq[ItemScore]) = {
    val randomIndex = rand.nextInt(items.length)
    val randomItem = items(randomIndex)

    (randomItem, forItem(randomItem))
  }

  def getUserHistory(userId: Id): List[String] =
    userHistory.get(userId).map(_._1).getOrElse(List.empty)
}
