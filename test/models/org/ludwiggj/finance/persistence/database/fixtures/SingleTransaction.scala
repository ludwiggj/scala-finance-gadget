package models.org.ludwiggj.finance.persistence.database.fixtures

import models.org.ludwiggj.finance.persistence.database.DatabaseLayer
import models.org.ludwiggj.finance.persistence.database.PKs.PK

trait SingleTransaction {
  this: DatabaseLayer =>

  val txUserANike140620 = txUserA("nike140620")

  val txId: PK[TransactionTable] = exec(Transactions.insert(txUserANike140620))
}