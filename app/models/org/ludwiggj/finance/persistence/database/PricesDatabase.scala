package models.org.ludwiggj.finance.persistence.database

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import models.org.ludwiggj.finance.domain.{FinanceDate, Price}
import play.api.db.DB
import play.api.Play.current
import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._
import models.org.ludwiggj.finance.persistence.database.Tables.{Funds, Prices, PricesRow}
import models.org.ludwiggj.finance.persistence.database.FundsDatabase.fundsRowWrapper

/**
 * Data access facade.
 */

// Note: declaring a Prices companion object would break the <> mapping.
object PricesDatabase {
  def apply() = new PricesDatabase()

  implicit def pricesRowWrapper(fundIdAndPrice: (Long, Price)) = {
    val (fundId, price) = fundIdAndPrice
    PricesRow(fundId, price.dateAsSqlDate, price.inPounds)
  }
}

class PricesDatabase {

  import PricesDatabase.pricesRowWrapper

  lazy val db = Database.forDataSource(DB.getDataSource("finance"))
  val prices: TableQuery[Prices] = TableQuery[Prices]
  val funds: TableQuery[Funds] = TableQuery[Funds]

  def get(fundName: String, priceDate: FinanceDate): Option[Price] = {
    db.withSession {
      implicit session =>

        val fetchedPrice = for {
          p <- prices if p.priceDate === priceDate.asSqlDate
          f <- funds if p.fundId === f.id && f.name === fundName
        } yield (fundName, priceDate.asSqlDate, p.price)

        fetchedPrice.list.headOption map {
          case (fundName, priceDate, price) => Price(fundName, priceDate, price)
        }
    }
  }

  def get(): List[Price] = {
    db.withSession {
      implicit session =>

        val fetchedPrices = for {
          p <- prices
          f <- funds if p.fundId === f.id
        } yield (f.name, p.priceDate, p.price)

        fetchedPrices.list map {
          case (fundName, priceDate, price) => Price(fundName, priceDate, price)
        }
    }
  }

  def insert(price: Price): Unit = {

    def insert(priceRow: PricesRow) = {
      db.withSession {
        implicit session =>

          def insert() = {
            try {
              prices += priceRow
            } catch {
              case ex: MySQLIntegrityConstraintViolationException =>
                println(s"Price: ${ex.getMessage} New price [$price]")
            }
          }

          get(price.holdingName, price.date) match {
            case Some(aPrice: Price) => ()
            case _ => insert()
          }
      }
    }

    val fundId = FundsDatabase().getOrInsert(price.holdingName)
    insert((fundId, price))
  }

  def insert(prices: List[Price]): Unit = {
    for (price <- prices) {
      insert(price)
    }
  }
}