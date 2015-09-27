package org.ludwiggj.finance.domain

import org.ludwiggj.finance.TestPrices
import org.scalatest.{FunSuite, Matchers}

class PriceSuite extends FunSuite with Matchers {

  test("Prices with dirty and cleaned up fund names are equal") {
    new TestPrices {
      priceBeforeNameCorrection should equal (priceAfterNameCorrection)
    }
  }
}