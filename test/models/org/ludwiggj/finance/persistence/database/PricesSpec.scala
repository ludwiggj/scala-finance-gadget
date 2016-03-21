package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.{FinanceDate, Price}
import models.org.ludwiggj.finance.persistence.database.Tables.FundsRow
import models.org.ludwiggj.finance.stringToSqlDate
import org.scalatest.{BeforeAndAfter, DoNotDiscover, Inside}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}

@DoNotDiscover
class PricesSpec extends PlaySpec with DatabaseHelpers with ConfiguredApp with BeforeAndAfter with Inside {

  before {
    DatabaseCleaner.recreateDb()
  }

  "get a single price" should {
    "return empty if price is not present" in {
      EmptySchema.loadData()

      PricesDatabase().get("fund that is not present", kappaPriceDate) must equal(None)
    }

    "return existing price if it is present" in {
      SinglePrice.loadData()

      PricesDatabase().get(kappaFundName, kappaPriceDate) mustBe Some(
        Price(kappaFundName, kappaPriceDate, kappaPriceInPounds)
      )
    }
  }

    "get a list of prices" should {
      "return the list" in {
        TwoPrices.loadData()

        PricesDatabase().get() must contain theSameElementsAs
          List(
            Price(kappaFundName, kappaPriceDate, kappaPriceInPounds),
            Price(nikeFundName, nikePriceDateGraeme, nikePriceInPoundsGraeme)
          )
      }

      "be unchanged if attempt to add price for same date" in {
        TwoPrices.loadData()

        val pricesDatabase = PricesDatabase()
        pricesDatabase.insert(Price(kappaFundName, kappaPriceDate, 2.12))
        pricesDatabase.get() must contain theSameElementsAs
          List(
            Price(kappaFundName, kappaPriceDate, kappaPriceInPounds),
            Price(nikeFundName, nikePriceDateGraeme, nikePriceInPoundsGraeme)
          )
      }

      "increase by one in length if add new unique price" in {
        TwoPrices.loadData()

        val pricesDatabase = PricesDatabase()
        pricesDatabase.insert(Price("holding1", FinanceDate("21/05/2014"), 2.12))
        pricesDatabase.get() must contain theSameElementsAs
          List(
            Price(kappaFundName, kappaPriceDate, kappaPriceInPounds),
            Price(nikeFundName, nikePriceDateGraeme, nikePriceInPoundsGraeme),
            Price("holding1", FinanceDate("21/05/2014"), 2.12)
          )
      }
    }

  "insert price" should {
    "insert fund if it is not present" in {
      EmptySchema.loadData()

      val pricesDatabase = PricesDatabase()

      FundsDatabase().get(capitalistsDreamFundName) mustBe None

      val capitalistsDreamFundPriceDate = FinanceDate("20/05/2014")
      val price = Price(capitalistsDreamFundName, capitalistsDreamFundPriceDate, 1.2)

      PricesDatabase().insert(price)

      inside(FundsDatabase().get(capitalistsDreamFundName).get) { case FundsRow(_, name) =>
        name must equal(capitalistsDreamFundName.name)
      }

      PricesDatabase().get(capitalistsDreamFundName, capitalistsDreamFundPriceDate) mustBe Some(price)
    }
  }

  "latestPrices" should {
    "return the latest price for each fund" in {
      MultiplePricesForTwoFunds.loadData()

      PricesDatabase().latestPrices("20/6/2014") must equal(
        Map(1L -> kappaPriceLater, 2L -> nikePriceGraemeLater)
      )
    }

    "omit a price if it is zero" in {
      MultiplePricesForSingleFund.loadData()

      PricesDatabase().latestPrices("16/5/2014") must equal(
        Map(1L -> kappaPriceEarliest)
      )
    }

    "omit a price if it is a two or more days too late" in {
      MultiplePricesForTwoFunds.loadData()

      PricesDatabase().latestPrices("19/6/2014") must equal(
        Map(1L -> kappaPriceLater, 2L -> nikePriceGraeme)
      )
    }

    "omit a fund if its earliest price is too late" in {
      MultiplePricesForTwoFunds.loadData()

      PricesDatabase().latestPrices("21/5/2014") must equal(
        Map(1L -> kappaPrice)
      )
    }

    "omit prices from name change fund if date of interest is more than one day before the fund change date" in {
      MultiplePricesForSingleFundAndItsRenamedEquivalent.loadData()

      PricesDatabase().latestPrices("22/5/2014") must equal(
        Map(1L -> kappaPriceLater)
      )
    }

    "include prices from name change fund if date of interest is one day before the fund change date" in {
      MultiplePricesForSingleFundAndItsRenamedEquivalent.loadData()

      val expectedUpdatedPrice = kappaIIPrice.copy(fundName = kappaFundName)

      PricesDatabase().latestPrices("23/5/2014") must equal(
        Map(1L -> expectedUpdatedPrice, 2L -> kappaIIPrice)
      )
    }

    "include prices from name change fund if date of interest is the fund change date" in {
      MultiplePricesForSingleFundAndItsRenamedEquivalent.loadData()
      val expectedUpdatedPrice = kappaIIPrice.copy(fundName = kappaFundName)

      PricesDatabase().latestPrices("24/5/2014") must equal(
        Map(1L -> expectedUpdatedPrice, 2L -> kappaIIPrice)
      )
    }
  }
}