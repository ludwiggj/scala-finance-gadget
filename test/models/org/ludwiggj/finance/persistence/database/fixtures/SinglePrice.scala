package models.org.ludwiggj.finance.persistence.database.fixtures

import models.org.ludwiggj.finance.persistence.database.DatabaseLayer
import models.org.ludwiggj.finance.persistence.database.PKs.PK

trait SinglePrice {

  this: DatabaseLayer =>

  val priceKappa = price("kappa140520")

  val existingPriceId: PK[PriceTable] = exec(Prices.insert(priceKappa))
}