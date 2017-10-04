package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.aLocalDate
import models.org.ludwiggj.finance.domain.FundName
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import fixtures._

import scala.math.BigDecimal.RoundingMode

class PortfolioListSpec extends PlaySpec with OneAppPerSuite with HasDatabaseConfigProvider[JdbcProfile] with BeforeAndAfter {

  private val nonExistentFund = FundName("fundThatIsNotPresent")

  lazy val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]

  before {
    val dbAPI = app.injector.instanceOf[DBApi]
    val defaultDatabase = dbAPI.database("default")
    Evolutions.cleanupEvolutions(defaultDatabase)
    Evolutions.applyEvolutions(defaultDatabase)

    MultipleTransactionsForTwoUsersAndTwoFunds.insert()
  }

  val databaseLayer = new DatabaseLayer(app.injector.instanceOf[DatabaseConfigProvider].get)

  import databaseLayer._

  val tolerance = BigDecimal(1e-3).bigDecimal

  object MultipleTransactionsForTwoUsersAndTwoFunds {
    val txUserANike140620 = txUserA("nike140620")

    val txUserANike140621 = txUserA("nike140621")
    val priceNike140621 = txUserANike140621.price

    private val txUserANike140625 = txUserA("nike140625")
    val priceNike140625 = txUserANike140625.price

    val txUserAKappa140520 = txUserA("kappa140520")
    val priceKappa140520 = txUserAKappa140520.price

    val txUserBNike140622 = txUserB("nike140622")
    val priceNike140622 = txUserBNike140622.price

    def insert() = {
      exec(Transactions.insert(List(
        txUserAKappa140520,
        txUserANike140620,
        txUserANike140621,
        txUserANike140625,
        txUserBNike140622,
        txUserB("nike140627")
      )))
    }
  }

  "portfolio list" should {
    "be based on all transactions up to and including date for both users" in {
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

    "be based on all transactions up to and including date for specific users" in {
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