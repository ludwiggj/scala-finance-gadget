package models.org.ludwiggj.finance.domain

import org.scalatest.{FunSuite, Matchers}

class CashDeltaSuite extends FunSuite with Matchers {

  test("Positive gain") {
    CashDelta(1, 2).gain should equal (1)
  }

  test("Negative gain") {
    CashDelta(2, 1).gain should equal (-1)
  }

  test("Zero gain") {
    CashDelta(1, 1).gain should equal (0.0)
  }

  test("Positive gain percentage") {
    CashDelta(1, 1.5).gainPct should equal (50.0)
  }

  test("Negative gain percentage") {
    CashDelta(1.5, 0.75).gainPct should equal (-50.0)
  }

  test("Gain percentage is zero when amount in is zero") {
    CashDelta(0, 1).gainPct should equal (0.0)
  }

  test("Add two CashDeltas together") {
    val addedDeltas = CashDelta(1, 2).add(CashDelta(1, 3))

    addedDeltas.gain should equal (3)
    addedDeltas.gainPct should equal (150.0)
  }
}