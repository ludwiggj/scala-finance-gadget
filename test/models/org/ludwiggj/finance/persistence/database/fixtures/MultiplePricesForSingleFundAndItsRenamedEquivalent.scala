package models.org.ludwiggj.finance.persistence.database.fixtures

import models.org.ludwiggj.finance.persistence.database.DatabaseLayer

trait MultiplePricesForSingleFundAndItsRenamedEquivalent {

  this: DatabaseLayer =>

  val priceKappa140523 = price("kappa140523")
  val priceKappaII140524 = price("kappaII140524")

  exec(Prices.insert(List(
    price("kappa140512"),
    price("kappa140516"),
    price("kappa140520"),
    priceKappa140523,
    priceKappaII140524
  )))
}