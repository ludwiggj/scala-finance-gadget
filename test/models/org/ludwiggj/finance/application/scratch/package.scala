package models.org.ludwiggj.finance.application

import java.sql.Date
import models.org.ludwiggj.finance.domain.{Price, Transaction}

package object scratch {
  type TransactionTuple = (String, Date, String, BigDecimal, BigDecimal, Date, BigDecimal, BigDecimal)

    // TODO: Moved this from Transaction().apply(...) so that TransactionTuple can also be moved
    // TODO: out of Transaction class and into this package. Ultimately TransactionTuple will be
    // TODO: deleted
    def transaction(userName:String, tx: TransactionTuple): Transaction = {
      val (fundName, date, description, in, out, priceDate, priceInPounds, units) = tx
      Transaction(userName, date, description, Option(in), Option(out), Price(fundName, priceDate, priceInPounds), units)
    }
}
