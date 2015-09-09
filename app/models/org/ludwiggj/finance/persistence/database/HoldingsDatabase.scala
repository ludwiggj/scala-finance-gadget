package models.org.ludwiggj.finance.persistence.database

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import models.org.ludwiggj.finance.domain.{Price, Holding}
import models.org.ludwiggj.finance.persistence.database.UsersDatabase.usersRowWrapper
import models.org.ludwiggj.finance.persistence.database.Tables._
import play.api.Play.current
import play.api.db.DB

import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._

/**
 * Data access facade.
 */

class HoldingsDatabase {
  lazy val db = Database.forDataSource(DB.getDataSource("finance"))

  def insert(userName: String, holding: Holding) = {
    db.withSession {
      implicit session =>

        val userId = UsersDatabase().getOrInsert(userName)

        def insert(fundId: Long) {
          try {
            Holdings += HoldingsRow(fundId, userId, holding.units, holding.priceDateAsSqlDate)
          } catch {
            case ex: MySQLIntegrityConstraintViolationException =>
              println(s"Holding: ${ex.getMessage}")
          }
        }

        PricesDatabase().insert(holding.price)

        FundsDatabase().get(holding.name) match {
          case Some(fundsRow) => insert(fundsRow.id)
          case _ => println(s"Could not insert Holding: fund ${holding.name} not found")
        }
    }
  }

  def insert(userName: String, holdings: List[Holding]): Unit = {
    for (holding <- holdings) {
      insert(userName, holding)
    }
  }

  implicit class HoldingExtension(q: Query[Holdings, HoldingsRow, Seq]) {
    def withFundsAndPrices = {
      q.join(Prices).on((h, p) => h.fundId === p.fundId && h.holdingDate === p.priceDate)
        .join(Funds).on((h_p, f) => h_p._1.fundId === f.id)
    }
  }

  implicit def asListOfHoldings(q: Query[((Holdings, Prices), Funds), ((HoldingsRow, PricesRow), FundsRow), Seq]): List[Holding] = {
    db.withSession {
      implicit session =>
        q.list map {
          case ((HoldingsRow(_, _, units, _), PricesRow(_, priceDate, price)), FundsRow(_, fundName)) =>
            Holding(Price(fundName, priceDate, price), units)
        }
    }
  }

  def get(): List[Holding] = {
    db.withSession {
      implicit session =>
        Holdings.withFundsAndPrices
    }
  }
}

// Note: declaring a Holdings companion object would break the <> mapping.
object HoldingsDatabase {
  def apply() = new HoldingsDatabase()
}