package org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.{Holding, Price}
import models.org.ludwiggj.finance.persistence.database.Tables.{UsersRow, FundsRow}
import models.org.ludwiggj.finance.persistence.database.{UsersDatabase, HoldingsDatabase, FundsDatabase, PricesDatabase}
import org.specs2.mutable.Specification

class HoldingsSpec extends Specification with DatabaseHelpers {

  "insert holding" should {
    "insert user, fund and price if they are not present" in EmptySchema {
      val usersDatabase = UsersDatabase()
      val fundsDatabase = FundsDatabase()
      val pricesDatabase = PricesDatabase()
      val holdingsDatabase = HoldingsDatabase()

      val userName = "Graeme"

      usersDatabase.get(userName) must beNone
      fundsDatabase.get(kappaFundName) must beNone
      pricesDatabase.get(kappaFundName, kappaFundPriceDate) must beNone

      holdingsDatabase.insert(kappaFundHolding)

      usersDatabase.get(userName) must beSome.which(
        _ match { case UsersRow(_, name) => name == userName })

      fundsDatabase.get(kappaFundName) must beSome.which(
        _ match { case FundsRow(_, name) => (name == kappaFundName.name) })

      pricesDatabase.get(kappaFundName, kappaFundPriceDate) must beSome(kappaFundPrice)

      holdingsDatabase.get() must containTheSameElementsAs(List(kappaFundHolding))
    }
  }

  "get a list of holdings" should {
    "be unchanged if attempt to add same holding for same user" in TwoHoldings {
      val holdingsDatabase = HoldingsDatabase()

      val kappaDuplicateHolding = Holding(
        userNameGraeme, Price(kappaFundName, kappaFundPriceDate, kappaFundPriceInPounds + 1), 1.23)

      holdingsDatabase.insert(kappaDuplicateHolding)

      holdingsDatabase.get() must containTheSameElementsAs(List(kappaFundHolding, nikeFundHolding))
    }
  }

  "get a list of holdings" should {
    "increase by one if attempt to add same holding for different user" in TwoHoldings {
      val holdingsDatabase = HoldingsDatabase()

      holdingsDatabase.get().size must beEqualTo(2)

      val kappaDuplicateHoldingForAnotherHolding = Holding(
        userNameAudrey, Price(kappaFundName, kappaFundPriceDate, kappaFundPriceInPounds + 1), 1.23)

      holdingsDatabase.insert(kappaDuplicateHoldingForAnotherHolding)

      holdingsDatabase.get().size must beEqualTo(3)
    }
  }
}