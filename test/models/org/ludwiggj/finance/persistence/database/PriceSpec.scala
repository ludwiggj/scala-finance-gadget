package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.aLocalDate
import models.org.ludwiggj.finance.domain.{FundName, Price}
import fixtures._
import org.scalatest.{BeforeAndAfter, Inside}
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.language.postfixOps

class PriceSpec extends PlaySpec with OneAppPerSuite with HasDatabaseConfigProvider[JdbcProfile] with BeforeAndAfter with Inside {

  lazy val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]

  before {
    val dbAPI = app.injector.instanceOf[DBApi]
    val defaultDatabase = dbAPI.database("default")
    Evolutions.cleanupEvolutions(defaultDatabase)
    Evolutions.applyEvolutions(defaultDatabase)
  }

  val databaseLayer = new DatabaseLayer(app.injector.instanceOf[DatabaseConfigProvider].get)

  import databaseLayer._

  object SinglePrice {
    val priceKappa = price("kappa140520")

    def insert() = {
      exec(Prices.insert(priceKappa))
    }
  }

  object MultiplePricesForSingleFund {
    val priceKappa140512 = price("kappa140512")

    def insert() = {
      exec(Prices.insert(List(
        priceKappa140512,
        price("kappa140516"),
        price("kappa140520"),
        price("kappa140523")
      )))
    }
  }

  object MultiplePricesForSingleFundAndItsRenamedEquivalent {
    val priceKappa140523 = price("kappa140523")
    val priceKappaII140524 = price("kappaII140524")

    def insert() = {
      exec(Prices.insert(List(
        price("kappa140512"),
        price("kappa140516"),
        price("kappa140520"),
        priceKappa140523,
        priceKappaII140524
      )))
    }
  }

  object MultiplePricesForTwoFunds {
    val priceKappa140520 = price("kappa140520")
    val priceKappa140523 = price("kappa140523")
    val priceNike140620 = price("nike140620")
    val priceNike140621 = price("nike140621")

    private val prices = List(
      priceKappa140520,
      priceKappa140523,
      priceNike140620,
      priceNike140621,
      price("nike140625")
    )

    def insert() = {
      exec(Prices.insert(prices))
    }
  }

  "get a single price" should {
    "return empty if price is not present" in {
      exec(Prices.get(FundName("fund that is not present"), aLocalDate("20/05/2014"))) must equal(None)
    }

    "return existing price if it is present" in {
      import SinglePrice._

      val priceId = SinglePrice.insert()
      val priceFundName = priceKappa.fundName
      val priceDate = priceKappa.date
      val priceAmount = priceKappa.inPounds

      inside(exec(Prices.get(priceFundName, priceDate))) {
        case Some(PriceRow(id, _, priceDate, priceInPounds)) =>
          id must equal(priceId)
          priceDate must equal(priceDate)
          priceInPounds must equal(priceAmount)
      }
    }

    "be unchanged if attempt to add price for same date" in {
      import SinglePrice._

      val priceId = SinglePrice.insert()
      val priceFundName = priceKappa.fundName
      val priceDate = priceKappa.date
      val priceAmount = priceKappa.inPounds

      exec(Prices.insert(priceKappa.copy(inPounds = 2.12)))

      inside(exec(Prices.get(priceFundName, priceDate))) {
        case Some(PriceRow(id, _, priceDate, priceInPounds)) =>
          id must equal(priceId)
          priceDate must equal(priceDate)
          priceInPounds must equal(priceAmount)
      }
    }
  }

  "insert price" should {
    "insert fund if it is not present" in {
      val newFundName = FundName("NewFund")

      exec(Funds.get(newFundName)) mustBe None

      val capitalistsDreamFundPriceDate = aLocalDate("20/05/2014")
      val price = Price(newFundName, capitalistsDreamFundPriceDate, 1.2)

      val priceId = exec(Prices.insert(price))

      inside(exec(Funds.get(newFundName)).get) { case FundRow(_, name) =>
        name must equal(newFundName)
      }

      inside(exec(Prices.get(newFundName, capitalistsDreamFundPriceDate))) {
        case Some(PriceRow(id, _, priceDate, priceInPounds)) =>
          id must equal(priceId)
          priceDate must equal(price.date)
          priceInPounds must equal(priceInPounds)
      }
    }
  }

  "latestPrices" when {

    "there are multiple prices for two funds" should {
      import MultiplePricesForTwoFunds._

      "return the latest price for each fund" in {
        MultiplePricesForTwoFunds.insert()

        exec(Prices.latestPrices(aLocalDate("20/06/2014"))).values.toList must contain theSameElementsAs
          List(priceKappa140523, priceNike140621)
      }

      "omit a price if it is a two or more days too late" in {
        MultiplePricesForTwoFunds.insert()

        exec(Prices.latestPrices(aLocalDate("19/06/2014"))).values.toList must contain theSameElementsAs
          List(priceKappa140523, priceNike140620)
      }

      "omit a fund if its earliest price is too late" in {
        MultiplePricesForTwoFunds.insert()

        exec(Prices.latestPrices(aLocalDate("21/05/2014"))).values.toList must contain theSameElementsAs
          List(priceKappa140520)
      }
    }

    "there are multiple prices for a single fund" should {
      import MultiplePricesForSingleFund._

      "omit a price if it is zero" in {
        MultiplePricesForSingleFund.insert()

        exec(Prices.latestPrices(aLocalDate("16/05/2014"))).values.toList must contain theSameElementsAs
          List(priceKappa140512)
      }
    }

    "there are multiple prices for a single fund whose name has changed" should {
      import MultiplePricesForSingleFundAndItsRenamedEquivalent._

      "omit prices from name change fund if date of interest is more than one day before the fund change date" in {
        MultiplePricesForSingleFundAndItsRenamedEquivalent.insert()

        exec(Prices.latestPrices(aLocalDate("22/05/2014"))).values.toList must contain theSameElementsAs
          List(priceKappa140523)
      }

      "include prices from name change fund if date of interest is one day before the fund change date" in {
        MultiplePricesForSingleFundAndItsRenamedEquivalent.insert()

        val expectedUpdatedPrice = price("kappaII140524").copy(fundName = FundName("Kappa"))

        exec(Prices.latestPrices(aLocalDate("23/05/2014"))).values.toList must contain theSameElementsAs
          List(expectedUpdatedPrice, priceKappaII140524)
      }

      "include prices from name change fund if date of interest is the fund change date" in {
        MultiplePricesForSingleFundAndItsRenamedEquivalent.insert()
        val expectedUpdatedPrice = price("kappaII140524").copy(fundName = FundName("Kappa"))

        exec(Prices.latestPrices(aLocalDate("24/05/2014"))).values.toList must contain theSameElementsAs
          List(expectedUpdatedPrice, priceKappaII140524)
      }
    }
  }
}