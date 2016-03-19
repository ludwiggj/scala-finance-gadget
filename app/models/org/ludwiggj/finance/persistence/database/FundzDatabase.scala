package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.FundName
import models.org.ludwiggj.finance.persistence.database.Tables._

import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._

trait FundzDatabase extends PlayConfigComp {
  class FundsDatabase {
    lazy val db = Database.forConfig("db.finance")

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

  object FundzDatabase {
    def apply() = new FundsDatabase()

    implicit def fundNameToFundsRow(fundName: FundName) = {
      FundsRow(0L, fundName)
    }
  }
}