package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.database.DatabaseLayer
import org.joda.time.LocalDate
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

case class PortfolioList(private val portfolios: List[Portfolio]) {
  val delta = portfolios.foldRight(CashDelta())(
    (portfolio, delta) => delta.add(portfolio.delta)
  )

  def iterator = portfolios.iterator
}

object PortfolioList {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  val db = dbConfig.db
  val databaseLayer = new DatabaseLayer(dbConfig.driver)
  import databaseLayer._
  import profile.api._

  def exec[T](action: DBIO[T]): T = Await.result(db.run(action), 2 seconds)

  def get(dateOfInterest: LocalDate): PortfolioList = {

    val transactions = exec(Transactions.getTransactionsUntil(dateOfInterest))

    val userNames = transactions.keys.map {
      _._1
    }.toList.distinct.sorted

    val portfolios = userNames map { userName =>
      Portfolio(userName, dateOfInterest, HoldingSummaryList(transactions, userName, dateOfInterest))
    }

    new PortfolioList(portfolios)
  }

  def get(dateOfInterest: LocalDate, userName: String): PortfolioList = {

    val transactions = exec(Transactions.getTransactionsUntil(dateOfInterest, userName))

    new PortfolioList(
      List(Portfolio(userName, dateOfInterest, HoldingSummaryList(transactions, userName, dateOfInterest)))
    )
  }
}