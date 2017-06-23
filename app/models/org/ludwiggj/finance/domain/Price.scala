package models.org.ludwiggj.finance.domain

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import models.org.ludwiggj.finance.persistence.database.Tables.{FundRow, FundTable, PriceRow, PriceTable, _}
import models.org.ludwiggj.finance.persistence.file.PersistableToFile
import org.joda.time.LocalDate
//TODO - refactor out!
import play.api.Play.current
import play.api.db.DB

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.BaseJoinQuery

case class Price(fundName: FundName, date: LocalDate, inPounds: BigDecimal) extends PersistableToFile {

  override def toString =
    s"Price [name: $fundName, date: ${FormattableLocalDate(date)}, price: £$inPounds]"

  def toFileFormat = s"$fundName$separator${FormattableLocalDate(date)}$separator$inPounds"
}

object Price {

  import models.org.ludwiggj.finance.LocalDateOrdering._
  import models.org.ludwiggj.finance.aLocalDate
  import models.org.ludwiggj.finance.persistence.database.PKs.PK

  def apply(row: Array[String]): Price = {
    val fundName = row(0)
    val priceDate = row(1)
    val priceInPounds = aBigDecimal(row(2))

    Price(fundName, priceDate, priceInPounds)
  }

  def apply(fundNameStr: String, priceDateStr: String, priceInPounds: BigDecimal): Price = {
    val fundName = FundName(fundNameStr)
    val priceDate = aLocalDate(priceDateStr)

    Price(fundName, priceDate, priceInPounds)
  }

  lazy val db = {
    def dbName = current.configuration.underlying.getString("db_name")
    Database.forDataSource(DB.getDataSource(dbName))
  }

  def insert(price: Price): Unit = {

    def insert(fundId: PK[FundTable]) = {
      db.withSession {
        implicit session =>

          def insert() = {
            try {
              Prices += PriceRow(fundId, price.date, price.inPounds)
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
    insert(fundId)
  }

  def insert(prices: List[Price]): Unit = {
    for (price <- prices) {
      insert(price)
    }
  }

  private def pricesOn(priceDate: LocalDate): Query[PriceTable, PriceRow, Seq] = {
    db.withSession {
      implicit session =>
        Prices filter (_.date === priceDate)
    }
  }

  type PriceFundQuery = BaseJoinQuery[PriceTable, FundTable, PriceRow, FundRow, Seq]
  type PriceFundQueryFilter = (PriceTable, FundTable) => Column[Boolean]

  private def getPriceList(priceFundQuery: PriceFundQuery, filter: PriceFundQueryFilter): List[Price] = {
    db.withSession {
      implicit session =>
        priceFundQuery.on((p, f) => filter(p, f)).list map {
          case ((PriceRow(_, priceDate, price), FundRow(_, fundName))) => Price(fundName, priceDate, price)
        }
    }
  }

  def get(): List[Price] = {
    getPriceList(
      Prices.join(Funds),
      (p, f) => p.fundId === f.id
    )
  }

  def get(fundName: FundName, priceDate: LocalDate): Option[Price] = {
    getPriceList(
      pricesOn(priceDate).join(Funds),
      (p, f) => (p.fundId === f.id) && (f.name === fundName)
    ).headOption
  }

  private val dateDifference = SimpleFunction.binary[LocalDate, LocalDate, Int]("DATEDIFF")

  private def latestPriceDates(dateOfInterest: LocalDate) = {
    db.withSession {
      implicit session =>
        Prices
//          .filter { p => ((dateDifference(p.date, dateOfInterest)) <= 1) && (p.price =!= BigDecimal(0.0)) }
          .filter { p => (p.price =!= BigDecimal(0.0)) }
          .groupBy(p => p.fundId)
          .map { case (fundId, group) => {
            (fundId, group.map(_.date).max)
          }
          }
    }
  }

  def latestPrices(dateOfInterest: LocalDate): Map[PK[FundTable], Price] = {

    def latestPrices: Map[PK[FundTable], Price] = {
      val pricesList = db.withSession {
        implicit session =>
          (for {
            (fundId, lastPriceDate) <- latestPriceDates(dateOfInterest)
            f <- Funds if f.id === fundId
            p <- Prices if p.fundId === fundId && p.date === lastPriceDate
          } yield (p.fundId, f.name, lastPriceDate, p.price)
            ).list
      }

      pricesList.foldLeft(Map[PK[FundTable], Price]()) {
        (m, priceInfo) => {
          val (fundId, fundName, lastPriceDate, price) = priceInfo
          m + (fundId -> Price(fundName, lastPriceDate.get, price))
        }
      }
    }

    def adjustLatestPricesForFundChanges(latestPrices: Map[PK[FundTable], Price]): Map[PK[FundTable], Price] = {
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