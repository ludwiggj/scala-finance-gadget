package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.data.TestHoldingSummary
import org.hamcrest.CoreMatchers.is
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.number.BigDecimalCloseTo.closeTo
import org.scalatest.{FunSuite, Matchers}

class HoldingSummarySuite extends FunSuite with Matchers with TestHoldingSummary {
  private val tolerance = BigDecimal(1e-3).bigDecimal

  test("HoldingSummary with standard transactions") {
    holdingSummary("aberdeen140625").amountIn should equal(450.50)
    holdingSummary("aberdeen140625").unitsIn should equal(281.1054)
    holdingSummary("aberdeen140625").unitsOut should equal(8.7910)
    holdingSummary("aberdeen140625").totalUnits should equal(272.3144)
    assertThat(holdingSummary("aberdeen140625").total.bigDecimal, is(closeTo(BigDecimal(384.590).bigDecimal, tolerance)))
  }

  test("HoldingSummary with conversion out") {
    holdingSummary("aberdeen141001").amountIn should equal(450.50)
    holdingSummary("aberdeen141001").unitsIn should equal(281.1054)
    holdingSummary("aberdeen141001").unitsOut should equal(281.1054)
    holdingSummary("aberdeen141001").totalUnits should equal(0)

    assertThat(holdingSummary("aberdeen141001").total.bigDecimal, is(closeTo(BigDecimal(0.0).bigDecimal, tolerance)))
  }

  test("HoldingSummary with conversion in") {
    holdingSummary("aberdeenB141001").amountIn should equal(800.00)
    holdingSummary("aberdeenB141001").unitsIn should equal(73.8407)
    holdingSummary("aberdeenB141001").unitsOut should equal(0.0)
    holdingSummary("aberdeenB141001").totalUnits should equal(73.8407)

    assertThat(holdingSummary("aberdeenB141001").total.bigDecimal, is(closeTo(BigDecimal(1270.658).bigDecimal, tolerance)))
  }

  test("Smaller than returns true if first holding summary is before the second holding summary") {
    holdingSummary("aberdeen140625") < holdingSummary("aberdeenB140901") should be(true)
  }

  test("Smaller than returns false if first holding summary is after the second holding summary") {
    holdingSummary("aberdeenB140901") < holdingSummary("aberdeen140625") should be(false)
  }

  test("Compare returns 0 if first and second holding summary are equal") {
    holdingSummary("aberdeen140625").compare(holdingSummary("aberdeen140625")) should equal(0)
  }
}