package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.data.{TestHoldings, userA}
import org.scalatest.{FunSuite, Matchers}

class HoldingSuite extends FunSuite with Matchers with TestHoldings {

  test("Holding.name is correct") {
    holding("aberdeen140502").name should equal(price("aberdeen140502").fundName)
  }

  test("Holding.priceDate is correct") {
    holding("aberdeen140502").priceDate should equal(price("aberdeen140502").date)
  }

  test("Holding.priceInPounds is correct") {
    holding("aberdeen140502").priceInPounds should equal(price("aberdeen140502").inPounds)
  }

  test("Holding.value is correct") {
    holding("aberdeen140502").value should equal(2666.33)
  }

  test("Holding.toFileFormat is correct") {
    holding("aberdeen140502").toFileFormat should equal(
      "Aberdeen Ethical World Equity A Fund Inc|1887.9336|02/05/2014|1.4123|2666.33"
    )
  }

  test("Holding.toString is correct") {
    holding("aberdeen140502").toString should equal(
      s"Financial Holding [userName: $userA, name: Aberdeen Ethical World Equity A Fund Inc, units: 1887.9336, "
        + "date: 02/05/2014, price: £1.4123, value: £2666.33]"
    )
  }

  test("Holding fund name should be cleaned up") {
    holding("schroder140520") shouldEqual holding("cleanedUp")
  }
}