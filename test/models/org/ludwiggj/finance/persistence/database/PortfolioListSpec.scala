package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.PortfolioList
import models.org.ludwiggj.finance.stringToSqlDate
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
      val portfolioList = PortfolioList.get("22/6/2014")

      portfolioList.iterator.size must equal(2)

      val portfolios = portfolioList.iterator
      val audreyPortfolio = portfolios.next()
      val audreyPortfolioDelta = audreyPortfolio.delta

      audreyPortfolioDelta.amountIn must equal(9.12)
      audreyPortfolioDelta.gain.setScale(2, RoundingMode.DOWN) must equal(0.0)
      audreyPortfolioDelta.gainPct.setScale(2, RoundingMode.DOWN) must equal(0.0)

      val graemePortfolio = portfolios.next()
      val graemePortfolioDelta = graemePortfolio.delta

      graemePortfolioDelta.amountIn must equal(285.6)
      graemePortfolioDelta.gain.setScale(2, RoundingMode.DOWN) must equal(-2.07)
      graemePortfolioDelta.gainPct.setScale(2, RoundingMode.DOWN) must equal(-0.72)
    }

    "be based on all transactions up to and including date for specific users" in {
      // Price will be latest price up to and including 25/6/14 i.e. 3.24 on 25/6/14
      val portfolioList = PortfolioList.get("24/6/2014", userNameAudrey)

      portfolioList.iterator.size must equal(1)

      val audreyPortfolio = portfolioList.iterator.next()
      val audreyPortfolioDelta = audreyPortfolio.delta

      audreyPortfolioDelta.amountIn must equal(9.12)
      audreyPortfolioDelta.gain.setScale(2, RoundingMode.DOWN) must equal(0.69)
      audreyPortfolioDelta.gainPct.setScale(2, RoundingMode.DOWN) must equal(7.64)
    }
  }
}