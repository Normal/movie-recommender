package als.calc

import als.MovieRating

import scala.util.Random

class RecommendationService(
                             scorer: Scorer,
                             items: Seq[Id],
                             users: Seq[Id],
                             userHistory: Map[Id, Seq[MovieRating]]
                           ) {

  val rand = new Random()

  def forUser(userId: Id): Seq[ItemScore] = {
    val userHistoryIds: Seq[Int] = userHistory.getOrElse(userId, List.empty).map(_.movieId)
    scorer.recommendationsForUser(userId, 20, userHistoryIds)
  }

  def forItem(itemId: Id): Seq[ItemScore] =
    scorer.recommendationsForItem(itemId, 20)

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

  def getUserHistory(userId: Id): Seq[MovieRating] =
    userHistory.getOrElse(userId, List.empty)
}
