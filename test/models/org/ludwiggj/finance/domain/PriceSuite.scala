package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.data.TestPrices
import org.scalatest.{FunSuite, Matchers}

class PriceSuite extends FunSuite with Matchers {

  test("Prices with dirty and cleaned up fund names are equal") {
    new TestPrices {
      priceBeforeNameCorrection should equal (priceAfterNameCorrection)
    }
  }
}