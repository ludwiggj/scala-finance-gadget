package org.ludwiggj.finance.domain

import java.sql.Date

import models.org.ludwiggj.finance.domain.FinanceDate
import org.ludwiggj.finance.TestHoldings
import org.scalatest.{FunSuite, Matchers}

class HoldingSuite extends FunSuite with Matchers {

  test("Holding.name is correct") {
    new TestHoldings {
      holding1.name equals ("Aberdeen Ethical World Equity A Fund Inc")
    }
  }

  test("Holding.priceDate is correct") {
    new TestHoldings {
      holding1.priceDate equals (FinanceDate("20/05/2014"))
    }
  }

  test("Holding.priceInPounds is correct") {
    new TestHoldings {
      holding1.priceInPounds equals (1.4360)
    }
  }

  test("Holding.units is correct") {
    new TestHoldings {
      holding1.units equals (1887.9336)
    }
  }

  test("Holding.value is correct") {
    new TestHoldings {
      holding1.value equals (2711.07)
    }
  }

  test("Holding.toFileFormat is correct") {
    new TestHoldings {
      holding1.toFileFormat equals (
        "Aberdeen Ethical World Equity A Fund Inc|1887.9336|20/05/2014|1.4360|2711.07"
        )
    }
  }

  test("Holding.toString is correct") {
    new TestHoldings {
      holding1.toString equals (
        "Financial Holding [name: Aberdeen Ethical World Equity A Fund Inc, units: 1887.9336, "
          + "date: 20/05/2014, price: £1.4360, value: £2711.07]"
        )
    }
  }
}