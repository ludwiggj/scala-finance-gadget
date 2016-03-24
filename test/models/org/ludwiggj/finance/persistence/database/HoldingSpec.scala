package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.{Fund, Holding, Price, User}
import Tables.{FundsRow, UsersRow}
import org.scalatest.{BeforeAndAfter, DoNotDiscover, Inside}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}

@DoNotDiscover
class HoldingSpec extends PlaySpec with DatabaseHelpers with ConfiguredApp with BeforeAndAfter with Inside {

  before {
    DatabaseCleaner.recreateDb()
  }

  "insert holding" should {
    "insert user, fund and price if they are not present" in {
      EmptySchema.loadData()

      val userName = "Graeme"

      User.get(userName) mustBe None
      Fund.get(kappaFundName) mustBe None
      Price.get(kappaFundName, kappaPriceDate) mustBe None

      Holding.insert(kappaFundHolding)

      inside (User.get(userName).get) { case UsersRow(_, name) =>
        name must equal(userName)
      }

      inside (Fund.get(kappaFundName).get) { case FundsRow(_, name) =>
        name must equal(kappaFundName.name)
      }

      Price.get(kappaFundName, kappaPriceDate) mustBe Some(kappaPrice)

      Holding.get() must contain theSameElementsAs List(kappaFundHolding)
    }
  }

  "get a list of holdings" should {
    "be unchanged if attempt to add same holding for same user" in {
      TwoHoldings.loadData()

      val kappaDuplicateHolding = Holding(
        userNameGraeme, Price(kappaFundName, kappaPriceDate, kappaPriceInPounds + 1), 1.23)

      Holding.insert(kappaDuplicateHolding)

      Holding.get() must contain theSameElementsAs List(kappaFundHolding, nikeFundHolding)
    }
  }

  "get a list of holdings" should {
    "increase by one if attempt to add same holding for different user" in {
      TwoHoldings.loadData()

      Holding.get().size must equal(2)

      val kappaDuplicateHoldingForAnotherHolding = Holding(
        userNameAudrey, Price(kappaFundName, kappaPriceDate, kappaPriceInPounds + 1), 1.23)

      Holding.insert(kappaDuplicateHoldingForAnotherHolding)

      Holding.get().size must equal(3)
    }
  }
}