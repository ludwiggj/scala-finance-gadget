package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.data.TestHoldingSummaryList
import org.hamcrest.CoreMatchers.is
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.number.BigDecimalCloseTo.closeTo
import org.scalatest.{FunSuite, Matchers}

class HoldingSummaryListSuite extends FunSuite with Matchers with TestHoldingSummaryList {
  private val tolerance = BigDecimal(1e-3).bigDecimal

  def assertHoldingSummary(holdingSummary: HoldingSummary, expectedAmountIn: BigDecimal, expectedUnitsIn: BigDecimal,
                           expectedUnitsOut: BigDecimal, expectedTotalUnits: BigDecimal, expectedTotal: BigDecimal): Unit = {
    holdingSummary.amountIn should equal(expectedAmountIn)
    holdingSummary.unitsIn should equal(expectedUnitsIn)
    holdingSummary.unitsOut should equal(expectedUnitsOut)
    holdingSummary.totalUnits should equal(expectedTotalUnits)
    assertThat(holdingSummary.total.bigDecimal, is(closeTo(BigDecimal(expectedTotal.bigDecimal).bigDecimal, tolerance)))
  }

  test("HoldingSummaryList should handle standard transactions") {
    val holdingSummaryListIterator = holdingSummaryList("aberdeen140915").iterator
    val holdingSummaryAberdeenA = holdingSummaryListIterator.next()
    val holdingSummaryAberdeenB = holdingSummaryListIterator.next()
    holdingSummaryListIterator.hasNext shouldBe (false)

    assertThat(holdingSummaryList("aberdeen140915").total.bigDecimal, is(closeTo(BigDecimal(1128.957).bigDecimal, tolerance)))
    assertHoldingSummary(holdingSummaryAberdeenA, 450.50, 281.1054, 8.7910, 272.3144, 384.589)
    assertHoldingSummary(holdingSummaryAberdeenB, 800.0, 48.8831, 0.0, 48.8831, 744.367)
  }

  test("HoldingSummaryList should handle fund changeovers") {
    val holdingSummaryListIterator = holdingSummaryList("aberdeen140930").iterator
    val holdingSummaryAberdeenA = holdingSummaryListIterator.next()
    val holdingSummaryAberdeenB = holdingSummaryListIterator.next()
    holdingSummaryListIterator.hasNext shouldBe (false)

    assertThat(holdingSummaryAberdeenA.total.bigDecimal, is(closeTo(BigDecimal(0.0).bigDecimal, tolerance)))
    assertHoldingSummary(holdingSummaryAberdeenA, 450.50, 281.1054, 281.1054, 0.0, 0.0)
    assertHoldingSummary(holdingSummaryAberdeenB, 800.0, 73.8407, 0.0, 73.8407, 1270.658)
  }

  test("HoldingSummaryList should handle fund changeovers and transfer income between funds") {
    val holdingSummaryListIterator = holdingSummaryList("aberdeen141001").iterator
    val holdingSummaryAberdeenB = holdingSummaryListIterator.next()
    holdingSummaryListIterator.hasNext shouldBe (false)

    assertThat(holdingSummaryList("aberdeen141001").total.bigDecimal, is(closeTo(BigDecimal(1270.658).bigDecimal, tolerance)))
    assertHoldingSummary(holdingSummaryAberdeenB, 1250.50, 73.8407, 0.0, 73.8407, 1270.658)
  }
}