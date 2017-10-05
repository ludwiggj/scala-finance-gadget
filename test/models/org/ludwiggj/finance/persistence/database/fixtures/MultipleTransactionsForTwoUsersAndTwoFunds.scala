package models.org.ludwiggj.finance.persistence.database.fixtures

import models.org.ludwiggj.finance.persistence.database.DatabaseLayer

trait MultipleTransactionsForTwoUsersAndTwoFunds {

  this: DatabaseLayer =>

  val txUserANike140620 = txUserA("nike140620")

  val txUserANike140621 = txUserA("nike140621")
  val priceNike140621 = txUserANike140621.price

  private val txUserANike140625 = txUserA("nike140625")
  val priceNike140625 = txUserANike140625.price

  val txUserAKappa140520 = txUserA("kappa140520")
  val priceKappa140520 = txUserAKappa140520.price

  val txUserBNike140622 = txUserB("nike140622")
  val priceNike140622 = txUserBNike140622.price

  exec(Transactions.insert(List(
    txUserAKappa140520,
    txUserANike140620,
    txUserANike140621,
    txUserANike140625,
    txUserBNike140622,
    txUserB("nike140627")
  )))
}