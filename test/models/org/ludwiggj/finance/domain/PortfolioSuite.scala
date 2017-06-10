package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.data.{TestHoldingSummaryList, userA}
import models.org.ludwiggj.finance.aLocalDate
import org.hamcrest.CoreMatchers.is
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.number.BigDecimalCloseTo.closeTo
import org.scalatest.{FunSuite, Matchers}

class PortfolioSuite extends FunSuite with Matchers with TestHoldingSummaryList {
  private val tolerance = BigDecimal(1e-3).bigDecimal

  test("Portfolio should calculate delta correctly") {
    val portfolioDelta = Portfolio(userA, aLocalDate("30/09/2014"), holdingSummaryList("aberdeen140930")).delta

    portfolioDelta.amountIn should equal(BigDecimal(1250.5))
    assertThat(portfolioDelta.total.bigDecimal, is(closeTo(BigDecimal(1270.65814967).bigDecimal, tolerance)))
    assertThat(portfolioDelta.gain.bigDecimal, is(closeTo(BigDecimal(20.15814967).bigDecimal, tolerance)))
    assertThat(portfolioDelta.gainPct.bigDecimal, is(closeTo(BigDecimal(1.612).bigDecimal, tolerance)))
  }
}