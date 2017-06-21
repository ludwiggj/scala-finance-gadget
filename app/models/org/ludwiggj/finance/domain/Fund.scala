package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.database.PKs.PK
import models.org.ludwiggj.finance.persistence.database.Tables._
//TODO - refactor out!
import play.api.Play.current
import play.api.db.DB
import scala.slick.driver.MySQLDriver.simple._

object Fund {

  lazy val db = {
    def dbName = current.configuration.underlying.getString("db_name")
    Database.forDataSource(DB.getDataSource(dbName))
  }

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

  def insert(fundName: FundName) = {
    db.withSession {
      implicit session =>
        (Funds returning Funds.map(_.id)) += FundRow(PK[FundTable](0L), fundName)
    }
  }

  def getOrInsert(fundName: FundName): PK[FundTable] = {
    get(fundName) match {
      case Some(aFund: FundRow) => aFund.id
      case _ => insert(fundName)
    }
  }
}