package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.{Price, Holding}
import models.org.ludwiggj.finance.persistence.database.UsersDatabase.stringToUsersRow
import models.org.ludwiggj.finance.persistence.database.Tables._
import play.api.Play.current
import play.api.db.DB

import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._

/**
 * Data access facade.
 */

class HoldingsDatabase private {
  lazy val db = Database.forDataSource(DB.getDataSource("finance"))

  def insert(holding: Holding) = {
    db.withSession {
      implicit session =>

        if (!get().contains(holding)) {

          val userId = UsersDatabase().getOrInsert(holding.userName)

          def insert(fundId: Long) {
            Holdings += HoldingsRow(fundId, userId, holding.units, holding.priceDate)
          }

          PricesDatabase().insert(holding.price)

          FundsDatabase().get(holding.name) match {
            case Some(fundsRow) => insert(fundsRow.id)
            case _ => println(s"Could not insert Holding: fund ${holding.name} not found")
          }
        }
    }
  }

  def insert(holdings: List[Holding]): Unit = {
    for (holding <- holdings) {
      insert(holding)
    }
  }

  implicit class HoldingExtension(q: Query[Holdings, HoldingsRow, Seq]) {
    def withFundsAndPricesAndUser = {
      q.join(Prices).on((h, p) => h.fundId === p.fundId && h.date === p.date)
        .join(Funds).on((h_p, f) => h_p._1.fundId === f.id)
        .join(Users).on((h_p_f, u) => h_p_f._1._1.userId === u.id)
    }
  }

  implicit def asListOfHoldings(q: Query[(((Holdings, Prices), Funds), Users),
    (((HoldingsRow, PricesRow), FundsRow), UsersRow), Seq]): List[Holding] = {
    db.withSession {
      implicit session =>
        q.list map {
          case (((HoldingsRow(_, _, units, _), PricesRow(_, priceDate, price)),
          FundsRow(_, fundName)), UsersRow(_, userName)) =>

            Holding(userName, Price(fundName, priceDate, price), units)
        }
    }
  }

  def get(): List[Holding] = {
    db.withSession {
      implicit session =>
        Holdings.withFundsAndPricesAndUser
    }
  }
}

// Note: declaring a Holdings companion object would break the <> mapping.
object HoldingsDatabase {
  def apply() = new HoldingsDatabase()
}