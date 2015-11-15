package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.{Portfolio, stringToSqlDate}
import org.specs2.matcher.MapMatchers
import org.specs2.mutable.Specification

import scala.math.BigDecimal.RoundingMode

class PortfolioSpec extends Specification with DatabaseHelpers with MapMatchers {

  "portfolio" should {
    "be based on all transactions up to and including date for both users" in MultipleTransactionsForTwoUsersAndTwoFunds {

      val tolerance = BigDecimal(1e-3).bigDecimal

      val portfolioList = Portfolio.get("22/6/2014")

      portfolioList.size must beEqualTo(2)

      val audreyPortfolio = portfolioList(0)
      val audreyPortfolioDelta = audreyPortfolio.delta

      audreyPortfolioDelta.amountIn shouldEqual (9.12)
      audreyPortfolioDelta.gain.setScale(2, RoundingMode.DOWN) shouldEqual (0.0)
      audreyPortfolioDelta.gainPct.setScale(2, RoundingMode.DOWN) shouldEqual (0.0)

      val graemePortfolio = portfolioList(1)
      val graemePortfolioDelta = graemePortfolio.delta

      graemePortfolioDelta.amountIn shouldEqual (285.6)
      graemePortfolioDelta.gain.setScale(2, RoundingMode.DOWN) shouldEqual (-2.07)
      graemePortfolioDelta.gainPct.setScale(2, RoundingMode.DOWN) shouldEqual (-0.72)
    }
  }
}