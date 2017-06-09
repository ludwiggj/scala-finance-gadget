package models.org.ludwiggj.finance.persistence

import models.org.ludwiggj.finance.domain.{FundName, Price, Transaction}
import models.org.ludwiggj.finance.persistence.database.PKs.PK
import models.org.ludwiggj.finance.persistence.database.Tables.{FundTable, PriceRow, TransactionRow}

import scala.language.implicitConversions

package object database {
  // TODO - Improve types, too many Strings here!!!
  type UserAndFund = (String, FundName)
  type TransactionsPerUserAndFund = Map[UserAndFund, (Seq[Transaction], Price)]
  type TransactionCandidates = Map[UserAndFund, Seq[(String, FundName, PK[FundTable], PriceRow, TransactionRow)]]
}