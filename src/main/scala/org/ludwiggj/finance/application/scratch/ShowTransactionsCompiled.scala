package org.ludwiggj.finance.application.scratch

import org.ludwiggj.finance.persistence.database.Tables.Prices

import scala.slick.driver.MySQLDriver.simple._

object ShowTransactionsCompiled extends App {

  def showTransactionsCompiled() {
    val prices: TableQuery[Prices] = TableQuery[Prices]

    val db = Database.forConfig("db")

    db.withSession {
      implicit session =>

        def fundPrices(fundName: Column[String]) = {
          (for {
            p <- prices
            f <- p.fundsFk if ((f.name === fundName))
          } yield (p.price, p.priceDate)).sortBy { case (_, date) => date }
        }

        val fundPricesCompiled = Compiled(fundPrices _)

        println(fundPrices("F&C Responsible UK Equity Growth 1 Fund Inc").list.last)
        println(fundPricesCompiled("F&C Responsible UK Equity Growth 1 Fund Inc").list.last)
    }
  }

  showTransactionsCompiled()
}