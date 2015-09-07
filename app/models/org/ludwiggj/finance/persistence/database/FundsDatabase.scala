package models.org.ludwiggj.finance.persistence.database

import play.api.Play.current
import play.api.db.DB

import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._
import models.org.ludwiggj.finance.persistence.database.Tables.{FundsRow, Funds}

/**
 * Data access facade.
 */

class FundsDatabase {
  lazy val db = Database.forDataSource(DB.getDataSource("finance"))
  val funds: TableQuery[Funds] = TableQuery[Funds]

  def get(fundName: String): Option[FundsRow] = {
    db.withSession {
      implicit session =>

        def getFundByName() =
          funds.filter {
            _.name === fundName
          }

        getFundByName().firstOption
    }
  }

  def insert(fund: FundsRow) = {
    db.withSession {
      implicit session =>
        (funds returning funds.map(_.id)) += fund
    }
  }

  def getOrInsert(fund: FundsRow): Long = {
    db.withSession {
      implicit session =>
        get(fund.name) match {
          case Some(aFund: FundsRow) => aFund.id
          case _ => insert(fund)
        }
    }
  }
}

// Note: declaring a Funds companion object would break the <> mapping.
object FundsDatabase {
  def apply() = new FundsDatabase()

  implicit def fundsRowWrapper(name: String) = FundsRow(0L, name)
}