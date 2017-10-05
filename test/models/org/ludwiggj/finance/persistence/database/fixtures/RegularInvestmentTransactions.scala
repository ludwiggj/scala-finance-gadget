package models.org.ludwiggj.finance.persistence.database.fixtures

import models.org.ludwiggj.finance.domain.{InvestmentRegular, Transaction}
import models.org.ludwiggj.finance.persistence.database.DatabaseLayer

trait RegularInvestmentTransactions {
  this: DatabaseLayer =>

  val priceNike150520 = price("nike150520")
  val priceNike140620 = price("nike140620")
  val priceNike140520 = price("nike140520")

  exec(Transactions.insert(List(
    Transaction(userA, priceNike140620.date, InvestmentRegular, Some(2.0), None, priceNike140620, 1.234),
    Transaction(userA, priceNike140520.date, InvestmentRegular, Some(2.0), None, priceNike140520, 1.34),
    Transaction(userA, priceNike150520.date, InvestmentRegular, Some(2.0), None, priceNike150520, 1.64),
    Transaction(userA, priceNike140520.date, InvestmentRegular, Some(5.0), None, priceNike140520, 1.34)
  )))
}