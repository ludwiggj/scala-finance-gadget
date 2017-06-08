package models.org.ludwiggj.finance.persistence

import models.org.ludwiggj.finance.domain.{Price, Transaction}
import scala.language.implicitConversions

package object database {
  // TODO - Type of second String should be FundName
  // TODO - Improve types, too many Strings here!!!
  type TransactionsKey = (String, String)
  type TransactionsPerUserAndFund = Map[TransactionsKey, (Seq[Transaction], Price)]
  type TransactionCandidates = Map[(String, String), Seq[(String, String, Long, Tables.PriceRow, Tables.TransactionRow)]]
}