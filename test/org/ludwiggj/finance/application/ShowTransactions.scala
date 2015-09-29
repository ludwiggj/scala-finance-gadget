package org.ludwiggj.finance.application

import models.org.ludwiggj.finance.domain.FinanceDate.sqlDateToFinanceDate
import play.api.Play
import play.api.Play.current
import play.api.db.DB
import play.api.test.FakeApplication
import scala.slick.driver.MySQLDriver.simple._
import models.org.ludwiggj.finance.persistence.database.Tables.{Users, Transactions}

object ShowTransactions extends App {

  def showTransactions() {
    val users: TableQuery[Users] = TableQuery[Users]
    val transactions: TableQuery[Transactions] = TableQuery[Transactions]

    lazy val db = Database.forDataSource(DB.getDataSource("finance"))

    db.withSession {
      implicit session =>

        def getTransactions(userName: String) = {
          (for {
            t <- transactions
            u <- t.usersFk if (u.name === userName)
            p <- t.pricesFk if ((t.priceDate === p.date) && (t.fundId === p.fundId))
            f <- t.fundsFk
          } yield (f.name, t.date, t.description, t.amountIn, t.amountOut, t.priceDate, p.price, t.units)
            ).sortBy(row => {
            val (fundName, transactionDate, _, _, _, _, _, _) = row
            (transactionDate, fundName)
          }).list
        }

        def allUsers() = {
          (for {
            userName <- users.map(_.name)
          } yield (userName)).list
        }

        for (userName <- allUsers()) {
          println(s"User $userName\n")
          println("Tx Date     Fund Name                                          Description               Cash In"
            + "   Cash Out  Price Date  Price       Units")
          println("-------     ---------                                          -----------               -------"
            + "   --------  ----------  -----       -----")
          getTransactions(userName) map {
            tx =>
              val (fundName, transactionDate, description, amountIn, amountOut, priceDate, price, units) = tx
              println(f"${sqlDateToFinanceDate(transactionDate)}  $fundName%-50s $description%-25s £$amountIn%7.2f  " +
                f"£$amountOut%7.2f  ${sqlDateToFinanceDate(priceDate)}  £$price%8.4f $units%10.4f")
          }
          println
        }
    }
  }

  private val application = FakeApplication()

  Play.start(application)

  showTransactions()

  Play.stop(application)
}