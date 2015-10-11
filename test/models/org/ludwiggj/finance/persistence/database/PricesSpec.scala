package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.{FinanceDate, Price}
import models.org.ludwiggj.finance.stringToSqlDate
import Tables.FundsRow
import org.specs2.mutable.Specification

class PricesSpec extends Specification with DatabaseHelpers {

  "get a single price" should {
    "return empty if price is not present" in EmptySchema {
      PricesDatabase().get("fund that is not present", kappaPriceDate) must beEqualTo(None)
    }

    "return existing price if it is present" in SinglePrice {
      PricesDatabase().get(kappaFundName, kappaPriceDate) must beSome(
        Price(kappaFundName, kappaPriceDate, kappaPriceInPounds)
      )
    }
  }

  "get a list of prices" should {
    "return the list" in TwoPrices {
      PricesDatabase().get() must containTheSameElementsAs(
        List(
          Price(kappaFundName, kappaPriceDate, kappaPriceInPounds),
          Price(nikeFundName, nikePriceDateGraeme, nikePriceInPoundsGraeme)
        ))
    }

    "be unchanged if attempt to add price for same date" in TwoPrices {
      val pricesDatabase = PricesDatabase()
      pricesDatabase.insert(Price(kappaFundName, kappaPriceDate, 2.12))
      pricesDatabase.get() must containTheSameElementsAs(
        List(
          Price(kappaFundName, kappaPriceDate, kappaPriceInPounds),
          Price(nikeFundName, nikePriceDateGraeme, nikePriceInPoundsGraeme)
        ))
    }

    "increase by one in length if add new unique price" in TwoPrices {
      val pricesDatabase = PricesDatabase()
      pricesDatabase.insert(Price("holding1", FinanceDate("21/05/2014"), 2.12))
      pricesDatabase.get() must containTheSameElementsAs(
        List(
          Price(kappaFundName, kappaPriceDate, kappaPriceInPounds),
          Price(nikeFundName, nikePriceDateGraeme, nikePriceInPoundsGraeme),
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

  "latestPrices" should {
    "return the latest price for each fund " in MultiplePricesForTwoFunds {
      PricesDatabase().latestPrices("20/6/2014") should beEqualTo(
        Map(1L -> kappaPriceLater, 2L -> nikePriceGraemeLater)
      )
    }
  }

  "latestPrices" should {
    "omit a price if it is a two or more days too late" in MultiplePricesForTwoFunds {
      PricesDatabase().latestPrices("19/6/2014") should beEqualTo(
        Map(1L -> kappaPriceLater, 2L -> nikePriceGraeme)
      )
    }
  }

  "latestPrices" should {
    "omit a fund if its earliest price is too late" in MultiplePricesForTwoFunds {
      PricesDatabase().latestPrices("21/5/2014") should beEqualTo(
        Map(1L -> kappaPrice)
      )
    }
  }
}