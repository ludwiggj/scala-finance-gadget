package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.aLocalDate
import models.org.ludwiggj.finance.persistence.database.fixtures.MultipleTransactionsForTwoUsersAndTwoFunds
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.math.BigDecimal.RoundingMode

class PortfolioListSpec extends PlaySpec with HasDatabaseConfigProvider[JdbcProfile] with GuiceOneAppPerSuite with BeforeAndAfter {

  before {
    // See https://stackoverflow.com/questions/31884182/play-2-4-2-play-slick-1-0-0-how-do-i-apply-database-evolutions-to-a-slick-man
    lazy val databaseApi = app.injector.instanceOf[DBApi]
    val defaultDatabase = databaseApi.database("default")
    cleanupEvolutions(defaultDatabase)
    applyEvolutions(defaultDatabase)
  }

  lazy val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]
  val tolerance = BigDecimal(1e-3).bigDecimal

  "the PortfolioList database API" should {
    "provide a get method," which {
      "returns a list based on all transactions up to and including date for both users" in
        new DatabaseLayer(dbConfig) with MultipleTransactionsForTwoUsersAndTwoFunds {
          val portfolioList = exec(PortfolioLists.get(aLocalDate("22/06/2014")))

          portfolioList.iterator.size must equal(2)

          val portfolios = portfolioList.iterator

          val userAPortfolio = portfolios.next()
          val userAPortfolioDelta = userAPortfolio.delta

          userAPortfolioDelta.amountIn must equal(285.6)
          userAPortfolioDelta.gain.setScale(2, RoundingMode.DOWN) must equal(-2.07)
          userAPortfolioDelta.gainPct.setScale(2, RoundingMode.DOWN) must equal(-0.72)

          val userBPortfolio = portfolios.next()
          val userBPortfolioDelta = userBPortfolio.delta

          userBPortfolioDelta.amountIn must equal(9.12)
          userBPortfolioDelta.gain.setScale(2, RoundingMode.DOWN) must equal(0.0)
          userBPortfolioDelta.gainPct.setScale(2, RoundingMode.DOWN) must equal(0.0)
        }

      "returns a list based on all transactions up to and including date for specific users" in
        new DatabaseLayer(dbConfig) with MultipleTransactionsForTwoUsersAndTwoFunds {
          // Price will be latest price up to and including 25/6/14 i.e. 3.24 on 25/6/14
          val portfolioList = exec(PortfolioLists.get(aLocalDate("24/06/2014"), "User B"))

          portfolioList.iterator.size must equal(1)

          val userBPortfolio = portfolioList.iterator.next()
          val userBPortfolioDelta = userBPortfolio.delta

          userBPortfolioDelta.amountIn must equal(9.12)
          userBPortfolioDelta.gain.setScale(2, RoundingMode.DOWN) must equal(0.69)
          userBPortfolioDelta.gainPct.setScale(2, RoundingMode.DOWN) must equal(7.64)
        }
    }
  }
}