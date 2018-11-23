package als.calc

import scala.util.Random

class RecommendationService(
                             scorer: CalculationEngine,
                             items: Seq[Id],
                             users: Seq[Id],
                             userHistory: Map[Id, (List[String], List[Int])]
                           ) {

  val rand = new Random()

  def forRandomUser: (Id, Seq[ItemScore]) = {
    val randomIndex = rand.nextInt(users.length)
    val randomUser = users(randomIndex)

    val userHistoryIds = userHistory.get(randomUser).map(_._2).getOrElse(List.empty)
    (randomUser, scorer.recommendationsForUser(randomUser, 10, userHistoryIds))
  }

  def forRandomItem: (Id, Seq[ItemScore]) = {
    val randomIndex = rand.nextInt(items.length)
    val randomItem = items(randomIndex)

    (randomItem, scorer.recommendationsForItem(randomItem, 10))
  }

  def getUserHistory(userId: Id): List[String] =
    userHistory.get(userId).map(_._1).getOrElse(List.empty)
}
