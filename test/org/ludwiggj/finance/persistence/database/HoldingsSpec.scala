package org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.{Holding, Price}
import models.org.ludwiggj.finance.persistence.database.Tables.FundsRow
import models.org.ludwiggj.finance.persistence.database.{HoldingsDatabase, FundsDatabase, PricesDatabase}
import org.specs2.mutable.Specification

class HoldingsSpec extends Specification with DatabaseHelpers {

  "insert a single holding" should {
    "work" in SinglePrice {

      println(HoldingsDatabase().get())
      true must beEqualTo(true)
    }

    "insert holding" should {
      "insert fund and price if they are not present" in EmptySchema {
        val fundsDatabase = FundsDatabase()
        val pricesDatabase = PricesDatabase()
        val holdingsDatabase = HoldingsDatabase()

        fundsDatabase.get(kappaFundName) must beNone
        pricesDatabase.get(kappaFundName, kappaFundPriceDate) must beNone

        holdingsDatabase.insert("Graeme", kappaFundHolding)

        fundsDatabase.get(kappaFundName) must beSome.which(
          _ match { case FundsRow(_, name) => name == kappaFundName })

        pricesDatabase.get(kappaFundName, kappaFundPriceDate) must beSome(kappaFundPrice)

        holdingsDatabase.get() must containTheSameElementsAs(List(kappaFundHolding))
      }
    }
  }

  "get a list of holdings" should {
    "be unchanged if attempt to add holding for same fund and date" in TwoHoldings {
      val holdingsDatabase = HoldingsDatabase()

      val kappaDuplicateHolding = Holding(Price(kappaFundName, kappaFundPriceDate, kappaFundPriceInPounds + 1), 1.23)

      holdingsDatabase.insert(userName, kappaDuplicateHolding)

      holdingsDatabase.get() must containTheSameElementsAs(List(kappaFundHolding, nikeFundHolding))
    }
  }
}