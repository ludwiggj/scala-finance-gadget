package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.data.TestHoldingSummaries
import models.org.ludwiggj.finance.data.userNameGraeme
import org.hamcrest.number.BigDecimalCloseTo.closeTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.is

import org.scalatest.{FunSuite, Matchers}

class HoldingSummariesSuite extends FunSuite with Matchers {
  private val tolerance = BigDecimal(1e-3).bigDecimal

  def assertHoldingSummary(holdingSummary: HoldingSummary, expectedAmountIn: BigDecimal, expectedUnitsIn: BigDecimal,
                           expectedUnitsOut: BigDecimal, expectedTotalUnits: BigDecimal, expectedTotal: BigDecimal): Unit = {
    holdingSummary.amountIn should equal(expectedAmountIn)
    holdingSummary.unitsIn should equal(expectedUnitsIn)
    holdingSummary.unitsOut should equal(expectedUnitsOut)
    holdingSummary.totalUnits should equal(expectedTotalUnits)
    assertThat(holdingSummary.total.bigDecimal, is(closeTo(BigDecimal(expectedTotal.bigDecimal).bigDecimal, tolerance)))
  }

  test("TestHoldingSummaries should handle standard transactions") {
    new TestHoldingSummaries {
      val aberdeenEthicalTransactionMap =
        Map(
          (userNameGraeme, aberdeenEthicalFundName) -> (aberdeenEthicalTransactions, aberdeenEthicalPrice140625),
          (userNameGraeme, newAberdeenEthicalFundName) -> (newAberdeenEthicalTransactions, newAberdeenEthicalPrice140901)
        )

      val holdingSummaries = HoldingSummaries(aberdeenEthicalTransactionMap, userNameGraeme, "15/09/2014")
      val holdingSummariesIterator = holdingSummaries.iterator
      val holdingSummaryAberdeenA = holdingSummariesIterator.next()
      val holdingSummaryAberdeenB = holdingSummariesIterator.next()
      holdingSummariesIterator.hasNext shouldBe(false)

      assertThat(holdingSummaries.total.bigDecimal, is(closeTo(BigDecimal(1128.957).bigDecimal, tolerance)))
      assertHoldingSummary(holdingSummaryAberdeenA, 450.50, 281.1054, 8.7910, 272.3144, 384.589)
      assertHoldingSummary(holdingSummaryAberdeenB, 800.0, 48.8831, 0.0, 48.8831, 744.367)
    }
  }

  test("TestHoldingSummaries should handle fund changeovers") {
    new TestHoldingSummaries {
      val holdingSummaries =
        HoldingSummaries(aberdeenEthicalWithConversionsTransactionMap, userNameGraeme, "30/09/2014")

      val holdingSummariesIterator = holdingSummaries.iterator
      val holdingSummaryAberdeenA = holdingSummariesIterator.next()
      val holdingSummaryAberdeenB = holdingSummariesIterator.next()
      holdingSummariesIterator.hasNext shouldBe(false)

      assertThat(holdingSummaryAberdeenA.total.bigDecimal, is(closeTo(BigDecimal(0.0).bigDecimal, tolerance)))
      assertHoldingSummary(holdingSummaryAberdeenA, 450.50, 281.1054, 281.1054, 0.0, 0.0)
      assertHoldingSummary(holdingSummaryAberdeenB, 800.0, 73.8407, 0.0, 73.8407, 1270.658)
    }
  }

  test("TestHoldingSummaries should handle fund changeovers and transfer income between funds") {
    new TestHoldingSummaries {
      val holdingSummaries =
        HoldingSummaries(aberdeenEthicalWithConversionsTransactionMap, userNameGraeme, "01/10/2014")

      val holdingSummariesIterator = holdingSummaries.iterator
      val holdingSummaryAberdeenB = holdingSummariesIterator.next()
      holdingSummariesIterator.hasNext shouldBe(false)

      assertThat(holdingSummaries.total.bigDecimal, is(closeTo(BigDecimal(1270.658).bigDecimal, tolerance)))
      assertHoldingSummary(holdingSummaryAberdeenB, 1250.50, 73.8407, 0.0, 73.8407, 1270.658)
    }
  }
}