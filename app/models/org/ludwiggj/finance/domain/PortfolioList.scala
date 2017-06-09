package models.org.ludwiggj.finance.domain

import org.joda.time.LocalDate
import scala.language.implicitConversions

case class PortfolioList(private val portfolios: List[Portfolio]) {
  val delta = portfolios.foldRight(CashDelta())(
    (portfolio, delta) => delta.add(portfolio.delta)
  )

  def iterator = portfolios.iterator
}

object PortfolioList {
  implicit def listOfPortfoliosToPortfolioList(portfolios: List[Portfolio]) = {
    new PortfolioList(portfolios)
  }

  def get(dateOfInterest: LocalDate): PortfolioList = {

    val transactions = Transaction.getTransactionsUntil(dateOfInterest)

    val userNames = transactions.keys.map {
      _._1
    }.toList.distinct.sorted

    userNames map { userName =>
      Portfolio(userName, dateOfInterest, HoldingSummaryList(transactions, userName, dateOfInterest))
    }
  }

  def get(dateOfInterest: LocalDate, userName: String): PortfolioList = {

    val transactions = Transaction.getTransactionsUntil(dateOfInterest, userName)

    List(Portfolio(userName, dateOfInterest, HoldingSummaryList(transactions, userName, dateOfInterest)))
  }
}