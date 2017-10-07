package models.org.ludwiggj.finance.persistence.database.fixtures

import models.org.ludwiggj.finance.persistence.database.DatabaseLayer

trait MultiplePricesForTwoFunds {

  this: DatabaseLayer =>

  val priceKappa140520 = price("kappa140520")
  val priceKappa140523 = price("kappa140523")
  val priceNike140620 = price("nike140620")
  val priceNike140621 = price("nike140621")

  exec(Prices.insert(List(
    priceKappa140520,
    priceKappa140523,
    priceNike140620,
    priceNike140621,
    price("nike140625")
  )))
}