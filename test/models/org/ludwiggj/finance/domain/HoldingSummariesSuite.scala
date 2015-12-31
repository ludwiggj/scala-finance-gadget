package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.data.TestHoldingSummaries
import models.org.ludwiggj.finance.data.userNameGraeme
import org.hamcrest.number.BigDecimalCloseTo.closeTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.is

import org.scalatest.{FunSuite, Matchers}

class HoldingSummariesSuite extends FunSuite with Matchers {
  private val tolerance = BigDecimal(1e-3).bigDecimal

  test("TestHoldingSummaries should handle standard transactions") {
    new TestHoldingSummaries {
      val aberdeenEthicalTransactionMap =
        Map(
          (userNameGraeme, aberdeenEthicalFundName) ->
            (aberdeenEthicalTransactions, aberdeenEthicalPrice140625),
          (userNameGraeme, newAberdeenEthicalFundName) ->
            (newAberdeenEthicalTransactions, newAberdeenEthicalPrice140901)
        )

      val holdingSummaries =
        HoldingSummaries(aberdeenEthicalTransactionMap, userNameGraeme, "15/09/2014", "testFundChanges")

      assertThat(holdingSummaries.total.bigDecimal, is(closeTo(BigDecimal(1128.957).bigDecimal, tolerance)))

      val holdingSummariesIterator = holdingSummaries.iterator

      val holdingSummaryAberdeenA = holdingSummariesIterator.next()

      holdingSummaryAberdeenA.amountIn should equal(450.50)
      holdingSummaryAberdeenA.unitsIn should equal(281.1054)
      holdingSummaryAberdeenA.unitsOut should equal(8.7910)
      holdingSummaryAberdeenA.totalUnits should equal(272.3144)
      assertThat(holdingSummaryAberdeenA.total.bigDecimal, is(closeTo(BigDecimal(384.589).bigDecimal, tolerance)))

      val holdingSummaryAberdeenB = holdingSummariesIterator.next()

      holdingSummaryAberdeenB.amountIn should equal(800.0)
      holdingSummaryAberdeenB.unitsIn should equal(48.8831)
      holdingSummaryAberdeenB.unitsOut should equal(0.0)
      holdingSummaryAberdeenB.totalUnits should equal(48.8831)
      assertThat(holdingSummaryAberdeenB.total.bigDecimal, is(closeTo(BigDecimal(744.367).bigDecimal, tolerance)))

      holdingSummariesIterator.hasNext shouldBe(false)
    }
  }

  test("TestHoldingSummaries should handle fund changeovers") {
    new TestHoldingSummaries {
      val holdingSummaries =
        HoldingSummaries(aberdeenEthicalWithConversionsTransactionMap, userNameGraeme, "30/09/2014", "testFundChanges")

      assertThat(holdingSummaries.total.bigDecimal, is(closeTo(BigDecimal(1270.658).bigDecimal, tolerance)))

      val holdingSummariesIterator = holdingSummaries.iterator

      val holdingSummaryAberdeenA = holdingSummariesIterator.next()

      holdingSummaryAberdeenA.amountIn should equal(450.50)
      holdingSummaryAberdeenA.unitsIn should equal(281.1054)
      holdingSummaryAberdeenA.unitsOut should equal(281.1054)
      holdingSummaryAberdeenA.totalUnits should equal(0.0)
      assertThat(holdingSummaryAberdeenA.total.bigDecimal, is(closeTo(BigDecimal(0.0).bigDecimal, tolerance)))

      val holdingSummaryAberdeenB = holdingSummariesIterator.next()

      holdingSummaryAberdeenB.amountIn should equal(800.0)
      holdingSummaryAberdeenB.unitsIn should equal(73.8407)
      holdingSummaryAberdeenB.unitsOut should equal(0.0)
      holdingSummaryAberdeenB.totalUnits should equal(73.8407)
      assertThat(holdingSummaryAberdeenB.total.bigDecimal, is(closeTo(BigDecimal(1270.658).bigDecimal, tolerance)))

      holdingSummariesIterator.hasNext shouldBe(false)
    }
  }

  test("TestHoldingSummaries should handle fund changeovers and transfer income between funds") {
    new TestHoldingSummaries {
      val holdingSummaries =
        HoldingSummaries(aberdeenEthicalWithConversionsTransactionMap, userNameGraeme, "01/10/2014", "testFundChanges")

      assertThat(holdingSummaries.total.bigDecimal, is(closeTo(BigDecimal(1270.658).bigDecimal, tolerance)))

      val holdingSummariesIterator = holdingSummaries.iterator

      val holdingSummaryAberdeenB = holdingSummariesIterator.next()

      holdingSummaryAberdeenB.amountIn should equal(1250.50)
      holdingSummaryAberdeenB.unitsIn should equal(73.8407)
      holdingSummaryAberdeenB.unitsOut should equal(0.0)
      holdingSummaryAberdeenB.totalUnits should equal(73.8407)
      assertThat(holdingSummaryAberdeenB.total.bigDecimal, is(closeTo(BigDecimal(1270.658).bigDecimal, tolerance)))

      holdingSummariesIterator.hasNext shouldBe(false)
    }
  }
}