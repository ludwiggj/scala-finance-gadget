package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.data.TestTransactionsAndPrice
import org.scalatest.{FunSuite, Matchers}

class HoldingSummarySuite extends FunSuite with Matchers {

  test("TestHoldingSummary with standard transactions") {
    new TestTransactionsAndPrice {
      val holdingSummary = HoldingSummary(transactions, price)

      holdingSummary.amountIn should equal(450.50)
      holdingSummary.unitsIn should equal(281.1054)
      holdingSummary.unitsOut should equal(8.7910)
      holdingSummary.totalUnits should equal(272.3144)
      holdingSummary.total === 495.58 +- 1e-3
    }
  }

  test("TestHoldingSummary with conversion out") {
    new TestTransactionsAndPrice {
      val holdingSummary = HoldingSummary(transactionsWithConversionOut, price)

      holdingSummary.amountIn should equal(450.50)
      holdingSummary.unitsIn should equal(281.1054)
      holdingSummary.unitsOut should equal(281.1054)
      holdingSummary.totalUnits should equal(0)
      holdingSummary.total === 0.0 +- 1e-3
    }
  }

  test("TestHoldingSummary with conversion in") {
    new TestTransactionsAndPrice {
      val holdingSummary = HoldingSummary(transactionsWithConversionIn, price)

      holdingSummary.amountIn should equal(450.50)
      holdingSummary.unitsIn should equal(1515.5375)
      holdingSummary.unitsOut should equal(8.7910)
      holdingSummary.totalUnits should equal(1506.7465)
      holdingSummary.total === 2742.12 +- 1e-3
    }
  }
}