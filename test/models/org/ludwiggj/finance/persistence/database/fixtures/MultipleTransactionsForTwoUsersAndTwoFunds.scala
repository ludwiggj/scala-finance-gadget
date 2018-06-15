package models.org.ludwiggj.finance.persistence.database.fixtures

import models.org.ludwiggj.finance.domain.Price
import models.org.ludwiggj.finance.persistence.database.DatabaseLayer

trait MultipleTransactionsForTwoUsersAndTwoFunds {

  this: DatabaseLayer =>

  val txUserANike140620 = txUserA("nike140620")

  val txUserANike140621 = txUserA("nike140621")
  val priceNike140621: Price = txUserANike140621.price

  private val txUserANike140625 = txUserA("nike140625")
  val priceNike140625: Price = txUserANike140625.price

  val txUserAKappa140520 = txUserA("kappa140520")
  val priceKappa140520: Price = txUserAKappa140520.price

  val txUserBNike140622 = txUserB("nike140622")
  val priceNike140622: Price = txUserBNike140622.price

  exec(Transactions.insert(List(
    txUserAKappa140520,
    txUserANike140620,
    txUserANike140621,
    txUserANike140625,
    txUserBNike140622,
    txUserB("nike140627")
  )))
}