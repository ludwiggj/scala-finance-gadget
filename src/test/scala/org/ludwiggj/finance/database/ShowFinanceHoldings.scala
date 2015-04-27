package org.ludwiggj.finance.database

import java.sql.Date
import org.ludwiggj.finance.database.Tables.{Funds, Holdings, Prices, Users}
import scala.slick.driver.MySQLDriver.simple._
import org.ludwiggj.finance.domain.FinanceDate
import scala.slick.jdbc.meta.MTable

object ShowFinanceHoldings extends App {
  def drop(tables: TableQuery[_ <: Table[_]]*)(implicit session: Session) {
    tables foreach {
      table =>
        if (!MTable.getTables(table.baseTableRow.tableName).list.isEmpty) table.ddl.drop
    }
  }

  def create(tables: TableQuery[_ <: Table[_]]*)(implicit session: Session) {
    tables foreach {
      _.ddl.create
    }
  }

  def showHoldings() {
    val users: TableQuery[Users] = TableQuery[Users]
    val funds: TableQuery[Funds] = TableQuery[Funds]
    val prices: TableQuery[Prices] = TableQuery[Prices]
    val holdings: TableQuery[Holdings] = TableQuery[Holdings]

    val db = Database.forConfig("db")

    db.withSession {
      implicit session =>

        def getHoldingDates() = {
          (for {
            h <- holdings
          } yield (h.holdingDate)
            ).sortBy(date => date).list.distinct
        }

        def getHoldings(userName: String, desiredDate: Date) = {
          (for {
            h <- holdings
            u <- h.usersFk if (u.name === userName)
            f <- h.fundsFk
            p <- prices if (p.fundId === f.id && p.priceDate === h.holdingDate && p.priceDate === desiredDate)
          } yield (f.name, h.holdingDate, h.units, p.price, h.units * p.price)
            ).list
        }

        def evaluateHoldings(userName: String, desiredDate: Date) = {
          println(s"Holdings for $userName on ${FinanceDate(desiredDate)}\n")

          val myHoldings = getHoldings(userName, desiredDate)

          println("Fund Name                                          Date        Units     Price       Total")
          println("---------                                          ----        -----     -----       -----")

          for (myHolding <- myHoldings) {
            val (fundName, holdingDate, units, sharePrice, total) = myHolding
            println(f"${fundName}%-50s ${FinanceDate(holdingDate)} ${units}%10.4f £${sharePrice}%8.4f £${(total)}%9.2f")
          }

          val totalHoldings = (myHoldings map { case (_, _, _, _, value) => value }).sum

          println(f"\nTotal holdings ($userName) £${totalHoldings}%9.2f\n")

          totalHoldings
        }

        def allUsers() = {
          (for {
            userName <- users.map(_.name)
          } yield (userName)).list
        }

        for (holdingDate <- getHoldingDates()) {
          val users = allUsers()

          val totalHoldingsForAllUsers = (for {
            user <- users
          } yield (evaluateHoldings(user, holdingDate))).sum

          println(f"Total holdings (${users.mkString(", ")}) £${totalHoldingsForAllUsers}%9.2f")
          println
        }
    }
  }

  showHoldings()
}

// Drop and recreate the schema
//      drop(prices, holdings, funds, users)
//      create(users, funds, holdings, prices)

//  new TestHoldings {
//    new DatabasePersister().persist("bart", holdings)
//  }