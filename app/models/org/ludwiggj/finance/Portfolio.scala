package models.org.ludwiggj.finance

import java.sql.Date

import models.org.ludwiggj.finance.domain._
import models.org.ludwiggj.finance.persistence.database.TransactionsDatabase

case class Portfolio(val userName: String, val date: FinanceDate, val holdings: List[HoldingSummary]) {
  val delta = CashDelta(holdings.map(_.amountIn).sum, holdings.map(_.total).sum)

  override def toString = holdings.foldLeft("")(
    (str: String, holdingSummary: HoldingSummary) => str + holdingSummary + "\n"
  )
}

object Portfolio {

  //TODO - need to write a test for this method
  def get(dateOfInterest: Date): List[Portfolio] = {

    val transactions = TransactionsDatabase().getTransactionsUpToAndIncluding(dateOfInterest)

    val userNames = transactions.keys.map {
      _._1
    }.toList.distinct

    val portfolios = userNames map { userName =>
      val usersHoldings: List[HoldingSummary] =
        transactions.filterKeys(_._1 == userName).values.toList.map(
          {
            case (txs: Seq[Transaction], price: Price) => HoldingSummary(txs.toList, price)
          }
        )

      Portfolio(userName, dateOfInterest, usersHoldings)
    }
    portfolios
  }
}