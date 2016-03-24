package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.database.Tables._
import play.api.Play.current
import play.api.db.DB
import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._


object Fund {
  implicit def fundNameToFundsRow(fundName: FundName) = FundsRow(0L, fundName)

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