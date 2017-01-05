package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.data.TestPrices
import org.scalatest.{FunSuite, Matchers}

class PriceSuite extends FunSuite with Matchers {

  test("Prices with dirty and cleaned up fund names are equal") {
    new TestPrices {
      priceBeforeNameCorrection should equal(priceAfterNameCorrection)
    }
  }

  test("Price.toFileFormat is correct") {
    new TestPrices {
      price1.toFileFormat should equal(
        "Henderson Global Care UK Income A Fund Inc|25/04/2010|0.8199"
      )
    }
  }
}