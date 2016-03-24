package models.org.ludwiggj.finance

import java.sql.Date

import models.org.ludwiggj.finance.domain._

case class Portfolio(val userName: String, val date: FinanceDate, private val holdings: HoldingSummaries) {
  val delta = CashDelta(holdings.amountIn, holdings.total)

  def holdingsIterator = holdings.iterator

  override def toString = holdings.toString
}

object Portfolio {
  def get(dateOfInterest: Date): List[Portfolio] = {

    val transactions = Transaction.getTransactionsUpToAndIncluding(dateOfInterest)

    val userNames = transactions.keys.map {
      _._1
    }.toList.distinct.sorted

    userNames map { userName =>
      Portfolio(userName, dateOfInterest, HoldingSummaries(transactions, userName, dateOfInterest))
    }
  }
}