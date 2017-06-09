package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.database.PKs.PK
import models.org.ludwiggj.finance.persistence.database.Tables._
import play.api.Play.current
import play.api.db.DB
import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._

object Fund {

  implicit def fundNameToFundsRow(fundName: FundName) = FundRow(PK[FundTable](0L), fundName)

  lazy val db = Database.forDataSource(DB.getDataSource("finance"))

  def get(fundName: FundName): Option[FundRow] = {
    db.withSession {
      implicit session =>

        def getFundByName() =
          Funds.filter {
            _.name === fundName
          }

        getFundByName().firstOption
    }
  }

  def getId(fundName: FundName): Option[PK[FundTable]] = {
    get(fundName).map(_.id)
  }

  def insert(fund: FundRow) = {
    db.withSession {
      implicit session =>
        (Funds returning Funds.map(_.id)) += fund
    }
  }

  def getOrInsert(fund: FundRow): PK[FundTable] = {
    db.withSession {
      implicit session =>
        get(fund.name) match {
          case Some(aFund: FundRow) => aFund.id
          case _ => insert(fund)
        }
    }
  }
}