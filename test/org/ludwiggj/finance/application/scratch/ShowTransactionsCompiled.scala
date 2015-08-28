package org.ludwiggj.finance.application.scratch

import models.org.ludwiggj.finance.persistence.database.Tables.Prices
import play.api.Play
import play.api.Play.current
import play.api.db.DB
import play.api.test.FakeApplication
import scala.slick.driver.MySQLDriver.simple._

object ShowTransactionsCompiled extends App {

  def showTransactionsCompiled() {
    val prices: TableQuery[Prices] = TableQuery[Prices]

    lazy val db = Database.forDataSource(DB.getDataSource("finance"))

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

  private val application = FakeApplication()

  Play.start(application)

  showTransactionsCompiled()

  Play.stop(application)
}