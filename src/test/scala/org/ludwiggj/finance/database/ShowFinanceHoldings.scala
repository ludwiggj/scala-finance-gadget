package org.ludwiggj.finance.database

import java.sql.Date
import org.ludwiggj.finance.database.Tables.{Funds, Holdings, Prices, Users}
import scala.slick.driver.MySQLDriver.simple._
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

  def showHoldings(desiredDate: Date) {
    val users: TableQuery[Users] = TableQuery[Users]
    val funds: TableQuery[Funds] = TableQuery[Funds]
    val prices: TableQuery[Prices] = TableQuery[Prices]
    val holdings: TableQuery[Holdings] = TableQuery[Holdings]

    val db = Database.forConfig("db")

    db.withSession {
      implicit session =>

        def getHoldings(userName: String) = {
          val joinQuery = for {
            h <- holdings
            u <- h.usersFk if (u.name === userName)
            f <- h.fundsFk
            p <- prices if (p.fundId === f.id && p.priceDate === h.dateOfRecord && p.priceDate === desiredDate)
          } yield (u.name, f.name, h.dateOfRecord, h.units, p.priceInPence)

          joinQuery.list.map {
            case (_, fund, date, units, priceInPence)
            => (fund, date, units, priceInPence, units * priceInPence / 100)
          }
        }

        def evaluateHoldings(userName: String) = {
          println(s"Holdings for $userName on $desiredDate")
          println

          val myHoldings = getHoldings(userName)

          for (myHolding <- myHoldings) {
            println(f"${myHolding._1}%-50s ${myHolding._2} " +
              f"${myHolding._3}%10.4f ${myHolding._4}%8.2f p  £${(myHolding._5)}%9.2f")
          }

          val totalHoldings = (myHoldings map { case (_, _, _, _, value) => value}).sum

          println
          println(f"Total holdings ($userName) £${totalHoldings}%9.2f")
          println

          totalHoldings
        }

        val allUsers = (for {
          userName <- users.map(_.name)
        } yield (userName)).list

        val totalHoldingsForAllUsers = (for {
          user <- allUsers
        } yield (evaluateHoldings(user))).sum

        println(f"Total holdings (${allUsers.mkString(", ")}) £${totalHoldingsForAllUsers}%9.2f")
    }
  }

  showHoldings(Date.valueOf("2015-01-28"))
}

/*

 */

// Drop and recreate the schema
//      drop(prices, holdings, funds, users)
//      create(users, funds, holdings, prices)

//  new TestHoldings {
//    new DatabasePersister().persist("bart", holdings)
//  }