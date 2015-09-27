package org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.{FinanceDate, Price}
import models.org.ludwiggj.finance.persistence.database.{FundsDatabase, PricesDatabase}
import models.org.ludwiggj.finance.persistence.database.Tables.FundsRow
import org.specs2.mutable.Specification

class PricesSpec extends Specification with DatabaseHelpers {

  "get a single price" should {
    "return empty if price is not present" in EmptySchema {
      PricesDatabase().get("fund that is not present", kappaFundPriceDate) must beEqualTo(None)
    }

    "return existing price if it is present" in SinglePrice {
      PricesDatabase().get(kappaFundName, kappaFundPriceDate) must beSome(
        Price(kappaFundName, kappaFundPriceDate, kappaFundPriceInPounds)
      )
    }
  }

  "get a list of prices" should {
    "return the list" in TwoPrices {
      PricesDatabase().get() must containTheSameElementsAs(
        List(
          Price(kappaFundName, kappaFundPriceDate, kappaFundPriceInPounds),
          Price(nikeFundName, nikeFundPriceDate, nikeFundPriceInPounds)
        ))
    }

    "be unchanged if attempt to add price for same date" in TwoPrices {
      val pricesDatabase = PricesDatabase()
      pricesDatabase.insert(Price(kappaFundName, kappaFundPriceDate, 2.12))
      pricesDatabase.get() must containTheSameElementsAs(
        List(
          Price(kappaFundName, kappaFundPriceDate, kappaFundPriceInPounds),
          Price(nikeFundName, nikeFundPriceDate, nikeFundPriceInPounds)
        ))
    }

    "increase by one in length if add new unique price" in TwoPrices {
      val pricesDatabase = PricesDatabase()
      pricesDatabase.insert(Price("holding1", FinanceDate("21/05/2014"), 2.12))
      pricesDatabase.get() must containTheSameElementsAs(
        List(
          Price(kappaFundName, kappaFundPriceDate, kappaFundPriceInPounds),
          Price(nikeFundName, nikeFundPriceDate, nikeFundPriceInPounds),
          Price("holding1", FinanceDate("21/05/2014"), 2.12)
        ))
    }
  }

  "insert price" should {
    "insert fund if it is not present" in EmptySchema {
      val pricesDatabase = PricesDatabase()

      FundsDatabase().get(capitalistsDreamFundName) must beNone

      val capitalistsDreamFundPriceDate = FinanceDate("20/05/2014")
      val price = Price(capitalistsDreamFundName, capitalistsDreamFundPriceDate, 1.2)

      PricesDatabase().insert(price)

      FundsDatabase().get(capitalistsDreamFundName) must beSome.which(
        _ match { case FundsRow(_, name) => (name == capitalistsDreamFundName.name) })

      PricesDatabase().get(capitalistsDreamFundName, capitalistsDreamFundPriceDate) must beSome(price)
    }
  }
}