package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.aLocalDate
import models.org.ludwiggj.finance.domain.{FundName, Price}
import models.org.ludwiggj.finance.persistence.database.fixtures._
import org.scalatest.BeforeAndAfter
import org.scalatest.Inside._
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions.{applyEvolutions, cleanupEvolutions}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.language.postfixOps

class PriceSpec extends PlaySpec with HasDatabaseConfigProvider[JdbcProfile] with OneAppPerSuite with BeforeAndAfter {

  before {
    // See https://stackoverflow.com/questions/31884182/play-2-4-2-play-slick-1-0-0-how-do-i-apply-database-evolutions-to-a-slick-man
    val databaseApi = app.injector.instanceOf[DBApi]
    val defaultDatabase = databaseApi.database("default")
    cleanupEvolutions(defaultDatabase)
    applyEvolutions(defaultDatabase)
  }

  lazy val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]

  "the Price database API" should {
    "provide a get method," which {
      "returns empty if the price is not present" in new DatabaseLayer(dbConfig) {
        exec(Prices.get(FundName("fund that is not present"), aLocalDate("20/05/2014"))) must equal(None)
      }

      "returns the existing price if it is present" in new DatabaseLayer(dbConfig) with SinglePrice {
        val priceFundName = priceKappa.fundName
        val priceDate = priceKappa.date
        val priceAmount = priceKappa.inPounds

        inside(exec(Prices.get(priceFundName, priceDate)).get) { case PriceRow(id, _, date, amount) =>
          (id, date, amount) must equal(existingPriceId, priceDate, priceAmount)
        }
      }

      "returns the existing price after an attempt to add the price again for the same date" in
        new DatabaseLayer(dbConfig) with SinglePrice {
          val priceFundName = priceKappa.fundName
          val priceDate = priceKappa.date
          val priceAmount = priceKappa.inPounds

          exec(Prices.insert(priceKappa.copy(inPounds = 2.12)))

          inside(exec(Prices.get(priceFundName, priceDate)).get) { case PriceRow(id, _, date, amount) =>
            (id, date, amount) must equal(existingPriceId, priceDate, priceAmount)
          }
        }
    }

    "provide an insert method," which {
      "inserts the fund referenced by the price if it is not present" in new DatabaseLayer(dbConfig) {
        val newFundName = FundName("NewFund")

        exec(Funds.get(newFundName)) mustBe None

        val capitalistsDreamFundPriceDate = aLocalDate("20/05/2014")
        val price = Price(newFundName, capitalistsDreamFundPriceDate, 1.2)

        val priceId = exec(Prices.insert(price))

        inside(exec(Funds.get(newFundName)).get) { case FundRow(_, name) =>
          name must equal(newFundName)
        }

        inside(exec(Prices.get(newFundName, capitalistsDreamFundPriceDate)).get) {
          case PriceRow(id, _, date, amount) =>
            (id, date, amount) must equal(priceId, price.date, price.inPounds)
        }
      }
    }

    "provide a latestPrices method," which {
      "returns the latest price for each fund" in new DatabaseLayer(dbConfig) with MultiplePricesForTwoFunds {
        exec(Prices.latestPrices(aLocalDate("20/06/2014"))).values.toList must contain theSameElementsAs
          List(priceKappa140523, priceNike140621)
      }

      "omits a price if it is a two or more days too late" in
        new DatabaseLayer(dbConfig) with MultiplePricesForTwoFunds {
          exec(Prices.latestPrices(aLocalDate("19/06/2014"))).values.toList must contain theSameElementsAs
            List(priceKappa140523, priceNike140620)
        }

      "omits a fund if its earliest price is too late" in new DatabaseLayer(dbConfig) with MultiplePricesForTwoFunds {
        exec(Prices.latestPrices(aLocalDate("21/05/2014"))).values.toList must contain theSameElementsAs
          List(priceKappa140520)
      }

      "omits a price if it is zero" in new DatabaseLayer(dbConfig) with MultiplePricesForSingleFund {
        exec(Prices.latestPrices(aLocalDate("16/05/2014"))).values.toList must contain theSameElementsAs
          List(priceKappa140512)
      }

      "omits prices from name change fund if date of interest is more than one day before the fund change date" in
        new DatabaseLayer(dbConfig) with MultiplePricesForSingleFundAndItsRenamedEquivalent {
          exec(Prices.latestPrices(aLocalDate("22/05/2014"))).values.toList must contain theSameElementsAs
            List(priceKappa140523)
        }

      "includes prices from name change fund if date of interest is one day before the fund change date" in {
        new DatabaseLayer(dbConfig) with MultiplePricesForSingleFundAndItsRenamedEquivalent {
          val expectedUpdatedPrice = price("kappaII140524").copy(fundName = FundName("Kappa"))

          exec(Prices.latestPrices(aLocalDate("23/05/2014"))).values.toList must contain theSameElementsAs
            List(expectedUpdatedPrice, priceKappaII140524)
        }
      }

      "includes prices from name change fund if date of interest is the fund change date" in {
        new DatabaseLayer(dbConfig) with MultiplePricesForSingleFundAndItsRenamedEquivalent {
          val expectedUpdatedPrice = price("kappaII140524").copy(fundName = FundName("Kappa"))

          exec(Prices.latestPrices(aLocalDate("24/05/2014"))).values.toList must contain theSameElementsAs
            List(expectedUpdatedPrice, priceKappaII140524)
        }
      }
    }
  }
}