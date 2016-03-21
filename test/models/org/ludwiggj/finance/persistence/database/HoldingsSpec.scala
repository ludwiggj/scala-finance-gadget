package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.{Holding, Price}
import Tables.{FundsRow, UsersRow}
import org.scalatest.{BeforeAndAfter, DoNotDiscover, Inside}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}

@DoNotDiscover
class HoldingsSpec extends PlaySpec with DatabaseHelpers with ConfiguredApp with BeforeAndAfter with Inside {

  before {
    DatabaseCleaner.recreateDb()
  }

  "insert holding" should {
    "insert user, fund and price if they are not present" in {
      EmptySchema.loadData()

      val usersDatabase = UsersDatabase()
      val fundsDatabase = FundsDatabase()
      val pricesDatabase = PricesDatabase()
      val holdingsDatabase = HoldingsDatabase()

      val userName = "Graeme"

      usersDatabase.get(userName) mustBe None
      fundsDatabase.get(kappaFundName) mustBe None
      pricesDatabase.get(kappaFundName, kappaPriceDate) mustBe None

      holdingsDatabase.insert(kappaFundHolding)

      inside (usersDatabase.get(userName).get) { case UsersRow(_, name) =>
        name must equal(userName)
      }

      inside (fundsDatabase.get(kappaFundName).get) { case FundsRow(_, name) =>
        name must equal(kappaFundName.name)
      }

      pricesDatabase.get(kappaFundName, kappaPriceDate) mustBe Some(kappaPrice)

      holdingsDatabase.get() must contain theSameElementsAs List(kappaFundHolding)
    }
  }

  "get a list of holdings" should {
    "be unchanged if attempt to add same holding for same user" in {
      TwoHoldings.loadData()

      val holdingsDatabase = HoldingsDatabase()

      val kappaDuplicateHolding = Holding(
        userNameGraeme, Price(kappaFundName, kappaPriceDate, kappaPriceInPounds + 1), 1.23)

      holdingsDatabase.insert(kappaDuplicateHolding)

      holdingsDatabase.get() must contain theSameElementsAs List(kappaFundHolding, nikeFundHolding)
    }
  }

  "get a list of holdings" should {
    "increase by one if attempt to add same holding for different user" in {
      TwoHoldings.loadData()

      val holdingsDatabase = HoldingsDatabase()

      holdingsDatabase.get().size must equal(2)

      val kappaDuplicateHoldingForAnotherHolding = Holding(
        userNameAudrey, Price(kappaFundName, kappaPriceDate, kappaPriceInPounds + 1), 1.23)

      holdingsDatabase.insert(kappaDuplicateHoldingForAnotherHolding)

      holdingsDatabase.get().size must equal(3)
    }
  }
}