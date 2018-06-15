package models.org.ludwiggj.finance.persistence.database.fixtures

import models.org.ludwiggj.finance.domain.Price
import models.org.ludwiggj.finance.persistence.database.DatabaseLayer

trait MultipleTransactionsForSingleUser {
  this: DatabaseLayer =>

  private val txUserANike140621 = txUserA("nike140621")
  val priceNike140621: Price = txUserANike140621.price

  private val txUserANike140625 = txUserA("nike140625")
  val priceNike140625: Price = txUserANike140625.price

  exec(Transactions.insert(List(
    txUserA("nike140620"),
    txUserANike140621,
    txUserANike140621,
    txUserANike140625
  )))
}