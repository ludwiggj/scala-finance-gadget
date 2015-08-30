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

  def getOrInsert(fund: FundsRow): Long = {
    val funds: TableQuery[Funds] = TableQuery[Funds]
    db.withSession {
      implicit session =>

        def insertFund() = {
          (funds returning funds.map(_.id)) += fund
        }

        val getFundByName = funds.filter {
          _.name === fund.name
        }

        if (getFundByName.exists.run) {
          getFundByName.first.id
        } else {
          insertFund()
        }
    }
  }
}

// Note: declaring a Funds companion object would break the <> mapping.
object FundsDatabase {
  def apply() = new FundsDatabase()

  implicit def fundsRowWrapper(name: String) = FundsRow(0L, name)
}