package models.org.ludwiggj.finance.persistence.database.fixtures

import models.org.ludwiggj.finance.persistence.database.DatabaseLayer

trait SinglePrice {

  this: DatabaseLayer =>

  val priceKappa = price("kappa140520")

  val existingPriceId = exec(Prices.insert(priceKappa))
}