package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.FundName
import models.org.ludwiggj.finance.domain.FundName.fundNameToString
import play.api.Play.current
import play.api.db.DB

import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._
import models.org.ludwiggj.finance.persistence.database.Tables.{FundsRow, Funds}


class FundsDatabase private {
  lazy val db = Database.forDataSource(DB.getDataSource("finance"))

  def get(fundName: FundName): Option[FundsRow] = {
    db.withSession {
      implicit session =>

        def getFundByName() =
          Funds.filter {
            _.name === fundName.name
          }

        getFundByName().firstOption
    }
  }

  def getId(fundName: FundName): Option[Long] = {
    get(fundName).map(_.id)
  }

  def insert(fund: FundsRow) = {
    db.withSession {
      implicit session =>
        (Funds returning Funds.map(_.id)) += fund
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

  implicit def fundNameToFundsRow(fundName: FundName) = FundsRow(0L, fundName)
}