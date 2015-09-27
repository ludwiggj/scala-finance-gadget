package org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.domain.{FundName, FinanceDate}
import org.ludwiggj.finance.TestHoldings
import org.scalatest.{FunSuite, Matchers}

class HoldingSuite extends FunSuite with Matchers {

  test("Holding.name is correct") {
    new TestHoldings {
      holding1.name should equal (FundName("Aberdeen Ethical World Equity A Fund Inc"))
    }
  }

  test("Holding.priceDate is correct") {
    new TestHoldings {
      holding1.priceDate should equal (FinanceDate("20/05/2014"))
    }
  }

  test("Holding.priceInPounds is correct") {
    new TestHoldings {
      holding1.priceInPounds should equal (1.4360)
    }
  }

  test("Holding.units is correct") {
    new TestHoldings {
      holding1.units should equal (1887.9336)
    }
  }

  test("Holding.value is correct") {
    new TestHoldings {
      holding1.value should equal (2711.07)
    }
  }

  test("Holding.toFileFormat is correct") {
    new TestHoldings {
      holding1.toFileFormat should equal (
        "Aberdeen Ethical World Equity A Fund Inc|1887.9336|20/05/2014|1.4360|2711.07"
        )
    }
  }

  test("Holding.toString is correct") {
    new TestHoldings {
      holding1.toString should equal (
        "Financial Holding [userName: Graeme, name: Aberdeen Ethical World Equity A Fund Inc, units: 1887.9336, "
          + "date: 20/05/2014, price: £1.4360, value: £2711.07]"
        )
    }
  }

  test("Holding fund name should be cleaned up") {
      new TestHoldings {
        holding3 shouldEqual(holding4)
      }
    }
}