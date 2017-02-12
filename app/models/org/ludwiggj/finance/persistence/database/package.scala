package models.org.ludwiggj.finance.persistence

import models.org.ludwiggj.finance.domain.{Price, Transaction}
import scala.language.implicitConversions

package object database {
  // TODO - Type of second String should be FundName
  type TransactionMapKey = (String, String)
  type TransactionMap = Map[TransactionMapKey, (Seq[Transaction], Price)]
  type TransactionsOfInterestType = Map[(String, String), Seq[(String, String, Long, Tables.PricesRow, Tables.TransactionsRow)]]
}