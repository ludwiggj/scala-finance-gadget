package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.Portfolio
import models.org.ludwiggj.finance.stringToSqlDate
import org.scalatest.{BeforeAndAfter, DoNotDiscover}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}

import scala.math.BigDecimal.RoundingMode

@DoNotDiscover
class PortfolioSpec extends PlaySpec with DatabaseHelpers with ConfiguredApp with BeforeAndAfter {

  before {
    DatabaseCleaner.recreateDb()
  }

  "portfolio" should {
    "be based on all transactions up to and including date for both users" in {

      MultipleTransactionsForTwoUsersAndTwoFunds.loadData()

      val tolerance = BigDecimal(1e-3).bigDecimal

      val portfolioList = Portfolio.get("22/6/2014")

      portfolioList.size must equal(2)

      val audreyPortfolio = portfolioList(0)
      val audreyPortfolioDelta = audreyPortfolio.delta

      audreyPortfolioDelta.amountIn must equal(9.12)
      audreyPortfolioDelta.gain.setScale(2, RoundingMode.DOWN) must equal(0.0)
      audreyPortfolioDelta.gainPct.setScale(2, RoundingMode.DOWN) must equal(0.0)

      val graemePortfolio = portfolioList(1)
      val graemePortfolioDelta = graemePortfolio.delta

      graemePortfolioDelta.amountIn must equal(285.6)
      graemePortfolioDelta.gain.setScale(2, RoundingMode.DOWN) must equal(-2.07)
      graemePortfolioDelta.gainPct.setScale(2, RoundingMode.DOWN) must equal(-0.72)
    }
  }
}