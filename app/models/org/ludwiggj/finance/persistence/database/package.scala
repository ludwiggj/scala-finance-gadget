package models.org.ludwiggj.finance.persistence

import models.org.ludwiggj.finance.domain.{FundName, Price, Transaction}

package object database {
  type UserName = String
  type UserNameAndFundName = (UserName, FundName)
  type TransactionsPerUserAndFund = Map[UserNameAndFundName, (Seq[Transaction], Price)]
}