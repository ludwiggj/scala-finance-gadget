package models.org.ludwiggj.finance.domain

import org.scalatest.{FunSuite, Matchers}

class FundNameSuite extends FunSuite with Matchers {

  test("FundName and cleaned up version are equal") {
    val fundNameBeforeNameCorrection = FundName(" ^Schroder Gbl Property Income Maximiser Z Fund Inc ")
    val fundNameAfterNameCorrection = FundName("Schroder Gbl Property Income Maximiser Z Fund Inc")

    fundNameBeforeNameCorrection should equal(fundNameAfterNameCorrection)
  }

  test("Smaller than returns true if first fund name is before the second alphabetically") {
    FundName("A") < FundName("B") should be(true)
  }

  test("Smaller than returns false if first fund name is after the second alphabetically") {
    FundName("B") < FundName("A") should be(false)
  }

  test("Compare returns 0 if first and second fund names are equal") {
    FundName("A").compare(FundName("A")) should equal(0)
  }
}