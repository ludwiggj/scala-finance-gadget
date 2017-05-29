package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.{FinanceDate, Fund, FundName, Price}
import models.org.ludwiggj.finance.persistence.database.Tables.FundsRow
import models.org.ludwiggj.finance.stringToSqlDate
import org.scalatest.{BeforeAndAfter, DoNotDiscover, Inside}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}

@DoNotDiscover
class PriceSpec extends PlaySpec with DatabaseHelpers with ConfiguredApp with BeforeAndAfter with Inside {

  before {
    Database.recreate()
  }

  "get a single price" should {
    "return empty if price is not present" in {
      EmptySchema.loadData()

      Price.get("fund that is not present", "20/05/2014") must equal(None)
    }

    "return existing price if it is present" in {
      SinglePrice.loadData()

      Price.get(price("kappa140520").fundName, price("kappa140520").date) mustBe Some(price("kappa140520"))
    }
  }

    "get a list of prices" should {
      "return the list" in {
        TwoPrices.loadData()

        Price.get() must contain theSameElementsAs List(price("kappa140520"), price("nike140620"))
      }

      "be unchanged if attempt to add price for same date" in {
        TwoPrices.loadData()

        Price.insert(price("kappa140520").copy(inPounds = 2.12))
        Price.get() must contain theSameElementsAs List(price("kappa140520"), price("nike140620"))
      }

      "increase by one in length if add new unique price" in {
        TwoPrices.loadData()

        val newPrice = Price("holding1", FinanceDate("21/05/2014"), 2.12)
        Price.insert(newPrice)
        Price.get() must contain theSameElementsAs List(price("kappa140520"), price("nike140620"), newPrice)
      }
    }

  "insert price" should {
    "insert fund if it is not present" in {
      EmptySchema.loadData()

      val newFundName: FundName = "NewFund"

      Fund.get(newFundName) mustBe None

      val capitalistsDreamFundPriceDate = "20/05/2014"
      val price = Price(newFundName, capitalistsDreamFundPriceDate, 1.2)

      Price.insert(price)

      inside(Fund.get(newFundName).get) { case FundsRow(_, name) =>
        name must equal(newFundName.name)
      }

      Price.get(newFundName, capitalistsDreamFundPriceDate) mustBe Some(price)
    }
  }

  "latestPrices" should {
    "return the latest price for each fund" in {
      MultiplePricesForTwoFunds.loadData()

      Price.latestPrices("20/6/2014") must equal(
        Map(1L -> price("kappa140523"), 2L -> price("nike140621"))
      )
    }

    "omit a price if it is zero" in {
      MultiplePricesForSingleFund.loadData()

      Price.latestPrices("16/5/2014") must equal(
        Map(1L -> price("kappa140512"))
      )
    }

    "omit a price if it is a two or more days too late" in {
      MultiplePricesForTwoFunds.loadData()

      Price.latestPrices("19/6/2014") must equal(
        Map(1L -> price("kappa140523"), 2L -> price("nike140620"))
      )
    }

    "omit a fund if its earliest price is too late" in {
      MultiplePricesForTwoFunds.loadData()

      Price.latestPrices("21/5/2014") must equal(
        Map(1L -> price("kappa140520"))
      )
    }

    "omit prices from name change fund if date of interest is more than one day before the fund change date" in {
      MultiplePricesForSingleFundAndItsRenamedEquivalent.loadData()

      Price.latestPrices("22/5/2014") must equal(
        Map(1L -> price("kappa140523"))
      )
    }

    "include prices from name change fund if date of interest is one day before the fund change date" in {
      MultiplePricesForSingleFundAndItsRenamedEquivalent.loadData()

      val expectedUpdatedPrice = price("kappaII140524").copy(fundName = "Kappa")

      Price.latestPrices("23/5/2014") must equal(
        Map(1L -> expectedUpdatedPrice, 2L -> price("kappaII140524"))
      )
    }

    "include prices from name change fund if date of interest is the fund change date" in {
      MultiplePricesForSingleFundAndItsRenamedEquivalent.loadData()
      val expectedUpdatedPrice = price("kappaII140524").copy(fundName = "Kappa")

      Price.latestPrices("24/5/2014") must equal(
        Map(1L -> expectedUpdatedPrice, 2L -> price("kappaII140524"))
      )
    }
  }
}