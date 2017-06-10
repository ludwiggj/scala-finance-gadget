package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.PortfolioList
import models.org.ludwiggj.finance.aLocalDate
import org.scalatest.{BeforeAndAfter, DoNotDiscover}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}
import scala.math.BigDecimal.RoundingMode

@DoNotDiscover
class PortfolioListSpec extends PlaySpec with DatabaseHelpers with ConfiguredApp with BeforeAndAfter {

  val tolerance = BigDecimal(1e-3).bigDecimal

  before {
    Database.recreate()
    MultipleTransactionsForTwoUsersAndTwoFunds.loadData()
  }

  "portfolio list" should {
    "be based on all transactions up to and including date for both users" in {
      val portfolioList = PortfolioList.get(aLocalDate("22/06/2014"))

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
      val portfolioList = PortfolioList.get(aLocalDate("24/06/2014"), "User B")

      portfolioList.iterator.size must equal(1)

      val userBPortfolio = portfolioList.iterator.next()
      val userBPortfolioDelta = userBPortfolio.delta

      userBPortfolioDelta.amountIn must equal(9.12)
      userBPortfolioDelta.gain.setScale(2, RoundingMode.DOWN) must equal(0.69)
      userBPortfolioDelta.gainPct.setScale(2, RoundingMode.DOWN) must equal(7.64)
    }
  }
}