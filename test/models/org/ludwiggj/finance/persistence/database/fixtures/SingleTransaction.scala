package models.org.ludwiggj.finance.persistence.database.fixtures

import models.org.ludwiggj.finance.persistence.database.DatabaseLayer

trait SingleTransaction {
  this: DatabaseLayer =>

  val txUserANike140620 = txUserA("nike140620")

  val txId = exec(Transactions.insert(txUserANike140620))
}