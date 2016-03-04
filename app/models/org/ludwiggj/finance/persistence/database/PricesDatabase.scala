package models.org.ludwiggj.finance.persistence.database

import java.sql.Date
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import models.org.ludwiggj.finance.domain.{FundChange, FundName, FinanceDate, Price}
import play.api.db.DB
import play.api.Play.current
import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._
import models.org.ludwiggj.finance.persistence.database.Tables.{Funds, Prices, PricesRow, FundsRow}
import models.org.ludwiggj.finance.persistence.database.FundsDatabase.fundNameToFundsRow
import models.org.ludwiggj.finance.dateTimeToSqlDate
import models.org.ludwiggj.finance.domain.FinanceDate.sqlDateToFinanceDate

class PricesDatabase private {

  implicit def fundIdAndPriceToPricesRow(fundIdAndPrice: (Long, Price)) = {
    val (fundId, price) = fundIdAndPrice
    PricesRow(fundId, price.date, price.inPounds)
  }

  implicit class PriceExtension(q: Query[Prices, PricesRow, Seq]) {
    def withFunds = q.join(Funds).on(_.fundId === _.id)

    def withFundsNamed(fundName: FundName) = q.join(Funds).on((p, f) => (p.fundId === f.id) && (f.name === fundName.name))
  }

  implicit def asListOfPrices(q: Query[(Prices, Funds), (PricesRow, FundsRow), Seq]): List[Price] = {
    db.withSession {
      implicit session =>
        q.list map {
          case ((PricesRow(_, priceDate, price), FundsRow(_, fundName))) => Price(fundName, priceDate, price)
        }
    }
  }

  lazy val db = Database.forDataSource(DB.getDataSource("finance"))

  def insert(price: Price): Unit = {

    def insert(priceRow: PricesRow) = {
      db.withSession {
        implicit session =>

          def insert() = {
            try {
              Prices += priceRow
            } catch {
              case ex: MySQLIntegrityConstraintViolationException =>
                println(s"Price: ${ex.getMessage} New price [$price]")
            }
          }

          get(price.fundName, price.date) match {
            case Some(aPrice: Price) => ()
            case _ => insert()
          }
      }
    }

    val fundId = FundsDatabase().getOrInsert(price.fundName)
    insert((fundId, price))
  }

  def insert(prices: List[Price]): Unit = {
    for (price <- prices) {
      insert(price)
    }
  }

  private def pricesOn(priceDate: FinanceDate): Query[Tables.Prices, Tables.PricesRow, Seq] = {
    db.withSession {
      implicit session =>
        Prices filter (_.date === dateTimeToSqlDate(priceDate.date))
    }
  }

  def get(): List[Price] = {
    Prices.withFunds
  }

  def get(fundName: FundName, priceDate: FinanceDate): Option[Price] = {
    pricesOn(priceDate).withFundsNamed(fundName).headOption
  }

  private val dateDifference = SimpleFunction.binary[Date, Date, Int]("DATEDIFF")

  private def latestPriceDates(dateOfInterest: Date) = {
    db.withSession {
      implicit session =>
        Prices
          .filter { p => ((dateDifference(p.date, dateOfInterest)) <= 1) && (p.price =!= BigDecimal(0.0)) }
          .groupBy(p => p.fundId)
          .map { case (fundId, group) => {
            (fundId, group.map(_.date).max)
          }
          }
    }
  }

  def latestPrices(dateOfInterest: Date): Map[Long, Price] = {

    def latestPrices: Map[Long, Price] = {
      val pricesList = db.withSession {
        implicit session =>
          (for {
            (fundId, lastPriceDate) <- latestPriceDates(dateOfInterest)
            f <- Funds if f.id === fundId
            p <- Prices if p.fundId === fundId && p.date === lastPriceDate
          } yield (p.fundId, f.name, lastPriceDate, p.price)
            ).list
      }

      pricesList.foldLeft(Map[Long, Price]()) {
        (m, priceInfo) => {
          val (fundId, fundName, lastPriceDate, price) = priceInfo
          m + (fundId -> Price(fundName, lastPriceDate.get, price))
        }
      }
    }

    def adjustLatestPricesForFundChanges(latestPrices: Map[Long, Price]): Map[Long, Price] = {
      val fundChangeMap = (for {
        fc <- FundChange.getFundChangesUpUntil(dateOfInterest.plusDays(1))
        fundIds <- fc.getFundIds
      } yield (fundIds)).toMap

      val adjustedPrices = for {
        (fundId, latestPrice) <- latestPrices
        newFundId <- fundChangeMap.get(fundId)
        newFundPrice <- latestPrices.get(newFundId)
      } yield if (newFundPrice.date > latestPrice.date) {
        (fundId, newFundPrice.copy(fundName = latestPrice.fundName))
      } else (fundId, latestPrice)

      val unadjustedPrices = latestPrices.filterKeys(!adjustedPrices.contains(_))

      adjustedPrices ++ unadjustedPrices
    }

    adjustLatestPricesForFundChanges(latestPrices)
  }
}

// Note: declaring a Prices companion object would break the <> mapping.
object PricesDatabase {
  def apply() = new PricesDatabase()
}