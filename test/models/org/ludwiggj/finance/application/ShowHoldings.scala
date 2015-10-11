package models.org.ludwiggj.finance.application

import java.sql.Date
import models.org.ludwiggj.finance.persistence.database.Tables.{Funds, Holdings, Prices, Users}
import play.api.Play
import play.api.Play.current
import play.api.db.DB
import play.api.test.FakeApplication
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta.MTable

object ShowHoldings extends App {
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

    lazy val db = Database.forDataSource(DB.getDataSource("finance"))

    db.withSession {
      implicit session =>

        def getHoldingDates() = {
          (for {
            h <- holdings
          } yield (h.date)
            ).sortBy(date => date).list.distinct
        }

        def getHoldings(userName: String, desiredDate: Date) = {
          (for {
            h <- holdings
            u <- h.usersFk if (u.name === userName)
            f <- h.fundsFk
            p <- prices if (p.fundId === f.id && p.date === h.date && p.date === desiredDate)
          } yield (f.name, h.date, h.units, p.price, h.units * p.price)
            ).list
        }

        def evaluateHoldings(userName: String, desiredDate: Date) = {
          println(s"Holdings for $userName on ${desiredDate}\n")

          val myHoldings = getHoldings(userName, desiredDate)

          println("Fund Name                                          Date        Units     Price       Total")
          println("---------                                          ----        -----     -----       -----")

          for (myHolding <- myHoldings) {
            val (fundName, holdingDate, units, sharePrice, total) = myHolding
            println(f"${fundName}%-50s ${holdingDate} ${units}%10.4f £${sharePrice}%8.4f £${(total)}%9.2f")
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

  private val application = FakeApplication()

  Play.start(application)

  showHoldings()

  Play.stop(application)
}

// Drop and recreate the schema
//      drop(prices, holdings, funds, users)
//      create(users, funds, holdings, prices)

//  new TestHoldings {
//    new DatabasePersister().persist("bart", holdings)
//  }