package models.org.ludwiggj.finance.persistence.database

import java.sql.Date

import models.org.ludwiggj.finance.domain.{Transaction, Price, Portfolio, HoldingSummary}

class Portfolios private {

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

object Portfolios {
  def apply() = new Portfolios()
}