package als.calc

import scala.util.Random

class RecommendationService(scorer: CalculationEngine, items: Seq[Id], users: Seq[Id]) {

  val rand = new Random()

  def forRandomUser: (Id, Seq[ItemScore]) = {
    val randomIndex = rand.nextInt(users.length)
    val randomUser = users(randomIndex)

    (randomUser, scorer.recommendationsForUser(randomUser, 10))
  }

  def forRandomItem: (Id, Seq[ItemScore]) = {
    val randomIndex = rand.nextInt(items.length)
    val randomItem = items(randomIndex)

    (randomItem, scorer.recommendationsForItem(randomItem, 10))
  }
}
