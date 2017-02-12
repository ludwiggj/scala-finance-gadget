package models.org.ludwiggj.finance.domain

import org.scalatest.{FunSuite, Matchers}

class CashDeltaSuite extends FunSuite with Matchers {

  test("Positive gain is correct") {
    CashDelta(1, 2).gain should equal (1)
  }

  test("Negative gain is correct") {
    CashDelta(2, 1).gain should equal (-1)
  }

  test("Zero gain percentage is correct") {
    CashDelta(1, 1).gainPct should equal (0.0)
  }

  test("Positive gain percentage is correct") {
    CashDelta(1, 1.5).gainPct should equal (50.0)
  }

  test("Negative gain percentage is correct") {
    CashDelta(1.5, 0.75).gainPct should equal (-50.0)
  }

  test("Gain percentage is zero when amount in is zero") {
    CashDelta(0, 1).gainPct should equal (0.0)
  }

  test("Should be able to add two CashDeltas together") {
    val addedDeltas = CashDelta(1, 2).add(CashDelta(1, 3))

    addedDeltas.gain should equal (3)
    addedDeltas.gainPct should equal (150.0)
  }
}