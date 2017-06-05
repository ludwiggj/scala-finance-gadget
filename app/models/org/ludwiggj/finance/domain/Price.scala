package models.org.ludwiggj.finance.domain

import java.sql.Date
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import models.org.ludwiggj.finance.domain.Fund.fundNameToFundsRow
import models.org.ludwiggj.finance.persistence.database.Tables
import models.org.ludwiggj.finance.persistence.database.Tables._
import models.org.ludwiggj.finance.persistence.file.PersistableToFile
import org.joda.time.LocalDate
import play.api.Play.current
import play.api.db.DB

import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._

case class Price(fundName: FundName, date: LocalDate, inPounds: BigDecimal) extends PersistableToFile {

  override def toString =
    s"Price [name: $fundName, date: ${FormattableLocalDate(date)}, price: £$inPounds]"

  def toFileFormat = s"$fundName$separator${FormattableLocalDate(date)}$separator$inPounds"
}

object Price {
  import models.org.ludwiggj.finance.{localDateToSqlDate, sqlDateToLocalDate, stringToLocalDate}
  import models.org.ludwiggj.finance.LocalDateOrdering._

  def apply(row: Array[String]): Price = {
    Price(row(0), row(1), row(2))
  }

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

    val fundId = Fund.getOrInsert(price.fundName)
    insert((fundId, price))
  }

  def insert(prices: List[Price]): Unit = {
    for (price <- prices) {
      insert(price)
    }
  }

  private def pricesOn(priceDate: LocalDate): Query[Tables.Prices, Tables.PricesRow, Seq] = {
    val priceSqlDate: Date = priceDate

    db.withSession {
      implicit session =>
        Prices filter (_.date === priceSqlDate)
    }
  }

  def get(): List[Price] = {
    Prices.withFunds
  }

  def get(fundName: FundName, priceDate: LocalDate): Option[Price] = {
    pricesOn(priceDate).withFundsNamed(fundName).headOption
  }

  private val dateDifference = SimpleFunction.binary[Date, Date, Int]("DATEDIFF")

  private def latestPriceDates(dateOfInterest: LocalDate) = {
    val theDateOfInterest: Date = dateOfInterest
    db.withSession {
      implicit session =>
        Prices
          .filter { p => ((dateDifference(p.date, theDateOfInterest)) <= 1) && (p.price =!= BigDecimal(0.0)) }
          .groupBy(p => p.fundId)
          .map { case (fundId, group) => {
            (fundId, group.map(_.date).max)
          }
          }
    }
  }

  def latestPrices(dateOfInterest: LocalDate): Map[Long, Price] = {

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
      val theDateOfInterest: LocalDate = dateOfInterest

      val fundChangeMap = (for {
        fc <- FundChange.getFundChangesUpUntil(theDateOfInterest.plusDays(1))
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