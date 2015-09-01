package models.org.ludwiggj.finance.persistence.database

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import models.org.ludwiggj.finance.domain.Price
import play.api.db.DB
import play.api.Play.current
import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._
import Tables.{Prices, PricesRow}

/**
 * Data access facade.
 */

class PricesDatabase {
  lazy val db = Database.forDataSource(DB.getDataSource("finance"))
  val prices: TableQuery[Prices] = TableQuery[Prices]

  def insert(price: PricesRow) = {

    db.withSession {
      implicit session =>

        val existingPrice = prices.filter { p =>
          (p.fundId === price.fundId) && (p.priceDate === price.priceDate)
        }

        def insertPrice() = {
          try {
            prices += price
          } catch {
            case ex: MySQLIntegrityConstraintViolationException =>
              println(s"Price: ${ex.getMessage} New price [$price]")
          }
        }

        if (existingPrice.exists.run) {
          ()
        } else {
          insertPrice()
        }
    }
  }

  def get() = {
    db.withSession {
      implicit session =>
        prices.list
    }
  }
}

// Note: declaring a Prices companion object would break the <> mapping.
object PricesDatabase {
  def apply() = new PricesDatabase()

  implicit def pricesRowWrapper(fundIdAndPrice: (Long, Price)) = {
    val (fundId, price) = fundIdAndPrice
    PricesRow(fundId, price.dateAsSqlDate, price.inPounds)
  }
}