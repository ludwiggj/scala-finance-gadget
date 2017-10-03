package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.database.DatabaseLayer
import org.joda.time.LocalDate
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.language.postfixOps

case class PortfolioList(private val portfolios: List[Portfolio]) {
  val delta = portfolios.foldRight(CashDelta())(
    (portfolio, delta) => delta.add(portfolio.delta)
  )

  def iterator = portfolios.iterator
}

object PortfolioList {

  val databaseLayer = new DatabaseLayer(DatabaseConfigProvider.get[JdbcProfile](Play.current))
  import databaseLayer._

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