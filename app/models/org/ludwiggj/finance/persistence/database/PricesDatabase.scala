package models.org.ludwiggj.finance.persistence.database

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import models.org.ludwiggj.finance.domain.{FinanceDate, Price}
import play.api.db.DB
import play.api.Play.current
import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._
import models.org.ludwiggj.finance.persistence.database.Tables.{Funds, Prices, PricesRow, FundsRow}
import models.org.ludwiggj.finance.persistence.database.FundsDatabase.fundsRowWrapper
import models.org.ludwiggj.finance.asSqlDate

/**
 * Data access facade.
 */

// Note: declaring a Prices companion object would break the <> mapping.
object PricesDatabase {
  def apply() = new PricesDatabase()
}

class PricesDatabase {

  implicit def pricesRowWrapper(fundIdAndPrice: (Long, Price)) = {
    val (fundId, price) = fundIdAndPrice
    PricesRow(fundId, price.date, price.inPounds)
  }

  implicit class PriceExtension(q: Query[Prices, PricesRow, Seq]) {
    def withFunds = q.join(Funds).on(_.fundId === _.id)

    def withFundsNamed(fundName: String) = q.join(Funds).on((p, f) => p.fundId === f.id && f.name === fundName)
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

  private def pricesOn(priceDate: FinanceDate): Query[Tables.Prices, Tables.PricesRow, Seq] = {
    db.withSession {
      implicit session =>
        Prices filter (_.priceDate === asSqlDate(priceDate.date))
    }
  }

  def get(): List[Price] = {
    Prices.withFunds
  }

  def get(fundName: String, priceDate: FinanceDate): Option[Price] = {
    pricesOn(priceDate).withFundsNamed(fundName).headOption
  }
}