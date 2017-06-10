package models.org.ludwiggj.finance.persistence

import models.org.ludwiggj.finance.domain.{FundName, Price, Transaction}
import models.org.ludwiggj.finance.persistence.database.PKs.PK
import models.org.ludwiggj.finance.persistence.database.Tables.{FundTable, PriceRow, TransactionRow}

package object database {
  type UserName = String
  type UserAndFundTuple = (UserName, FundName)
  type TransactionsPerUserAndFund = Map[UserAndFundTuple, (Seq[Transaction], Price)]
  type TransactionCandidates = Map[UserAndFundTuple, Seq[(UserName, FundName, PK[FundTable], PriceRow, TransactionRow)]]
}