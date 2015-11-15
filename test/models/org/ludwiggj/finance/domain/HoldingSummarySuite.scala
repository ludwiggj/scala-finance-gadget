package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.data.TestHoldingSummary
import org.scalatest.{FunSuite, Matchers}
import org.hamcrest.number.BigDecimalCloseTo.closeTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.is

class HoldingSummarySuite extends FunSuite with Matchers {
  private val tolerance = BigDecimal(1e-3).bigDecimal

  test("TestHoldingSummary with standard transactions") {
    new TestHoldingSummary {
      aberdeenEthicalHoldingSummary.amountIn should equal(450.50)
      aberdeenEthicalHoldingSummary.unitsIn should equal(281.1054)
      aberdeenEthicalHoldingSummary.unitsOut should equal(8.7910)
      aberdeenEthicalHoldingSummary.totalUnits should equal(272.3144)

      assertThat(aberdeenEthicalHoldingSummary.total.bigDecimal, is(closeTo(BigDecimal(384.590).bigDecimal, tolerance)))
    }
  }

  test("TestHoldingSummary with conversion out") {
    new TestHoldingSummary {
      aberdeenEthicalHoldingSummaryWithConversionOut.amountIn should equal(450.50)
      aberdeenEthicalHoldingSummaryWithConversionOut.unitsIn should equal(281.1054)
      aberdeenEthicalHoldingSummaryWithConversionOut.unitsOut should equal(281.1054)
      aberdeenEthicalHoldingSummaryWithConversionOut.totalUnits should equal(0)

      assertThat(aberdeenEthicalHoldingSummaryWithConversionOut.total.bigDecimal, is(closeTo(BigDecimal(0.0).bigDecimal, tolerance)))
    }
  }

  test("TestHoldingSummary with conversion in") {
    new TestHoldingSummary {
      aberdeenEthicalHoldingSummaryWithConversionIn.amountIn should equal(450.50)
      aberdeenEthicalHoldingSummaryWithConversionIn.unitsIn should equal(1515.5375)
      aberdeenEthicalHoldingSummaryWithConversionIn.unitsOut should equal(8.7910)
      aberdeenEthicalHoldingSummaryWithConversionIn.totalUnits should equal(1506.7465)

      assertThat(aberdeenEthicalHoldingSummaryWithConversionIn.total.bigDecimal, is(closeTo(BigDecimal(2127.978).bigDecimal, tolerance)))
    }
  }

  test("Smaller than returns true if first holding summary is before the second holding summary") {
    new TestHoldingSummary {
      aberdeenEthicalHoldingSummary < newAberdeenEthicalHoldingSummary should be(true)
    }
  }

  test("Smaller than returns false if first holding summary is after the second holding summary") {
    new TestHoldingSummary {
      newAberdeenEthicalHoldingSummary < aberdeenEthicalHoldingSummary should be(false)
    }
  }

  test("Compare returns 0 if first and second holding summary are equal") {
    new TestHoldingSummary {
      aberdeenEthicalHoldingSummary.compare(aberdeenEthicalHoldingSummary) should equal(0)
    }
  }
}