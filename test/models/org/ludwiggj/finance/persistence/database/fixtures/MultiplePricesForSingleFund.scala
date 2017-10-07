package models.org.ludwiggj.finance.persistence.database.fixtures

import models.org.ludwiggj.finance.persistence.database.DatabaseLayer

trait MultiplePricesForSingleFund {

  this: DatabaseLayer =>

  val priceKappa140512 = price("kappa140512")

  exec(Prices.insert(List(
    priceKappa140512,
    price("kappa140516"),
    price("kappa140520"),
    price("kappa140523")
  )))
}