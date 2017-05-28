package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.data.TestPrices
import org.scalatest.{FunSuite, Matchers}

class PriceSuite extends FunSuite with Matchers with TestPrices {

  test("Prices with dirty and cleaned up fund names are equal") {
    val priceBeforeNameCorrection = Price("^Schroder Gbl Property Income Maximiser Z Fund Inc", "25/12/2014", "0.4799")
    val priceAfterNameCorrection = Price("Schroder Gbl Property Income Maximiser Z Fund Inc", "25/12/2014", "0.4799")
    priceBeforeNameCorrection should equal(priceAfterNameCorrection)
  }

  test("Price.toFileFormat is correct") {
    price("aberdeen140502").toFileFormat should equal(
      "Aberdeen Ethical World Equity A Fund Inc|02/05/2014|1.4123"
    )
  }
}