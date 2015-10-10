package org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.domain.HoldingSummary
import org.ludwiggj.finance.TestTransactionsAndPrice
import org.scalatest.{FunSuite, Matchers}

class HoldingSummarySuite extends FunSuite with Matchers {

  test("TestHoldingSummary test") {
    new TestTransactionsAndPrice {
      val holdingSummary = HoldingSummary(transactions, price)

      holdingSummary.amountIn should equal(450.50)
      holdingSummary.unitsIn should equal(281.1054)
      holdingSummary.unitsOut should equal(8.7910)
      holdingSummary.totalUnits should equal(272.3144)
      holdingSummary.total === 495.58 +- 1e-3
    }
  }
}