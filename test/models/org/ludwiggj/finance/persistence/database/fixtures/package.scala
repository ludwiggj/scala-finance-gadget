package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.{InvestmentRegular, Price, SaleForRegularPayment, Transaction}

package object fixtures {
  val userA = "User A"
  val userB = "User B"

  val price: Map[String, Price] = Map(
    "kappa140512" -> Price("Kappa", "12/05/2014", 2.50),
    "kappa140516" -> Price("Kappa", "16/05/2014", 0.0),
    "kappa140520" -> Price("Kappa", "20/05/2014", 1695.055169),
    "kappa140523" -> Price("Kappa", "23/05/2014", 1.65),
    "kappaII140524" -> Price("Kappa II", "24/05/2014", 1.66),
    "nike140520" -> Price("Nike", "20/05/2014", 2.97),
    "nike140620" -> Price("Nike", "20/06/2014", 3.12245),
    "nike140621" -> Price("Nike", "21/06/2014", 3.08),
    "nike140622" -> Price("Nike", "22/06/2014", 3.01),
    "nike140625" -> Price("Nike", "25/06/2014", 3.24),
    "nike140627" -> Price("Nike", "27/06/2014", 3.29),
    "nike150520" -> Price("Nike", "20/05/2015", 3.28)
  )

  val txUserA: Map[String, Transaction] = Map(
    "kappa140520" -> Transaction(
      userA, price("kappa140520").date, InvestmentRegular, Some(282.1), None, price("kappa140520"), 251.875),
    "nike140620" -> Transaction(
      userA, price("nike140620").date, InvestmentRegular, Some(3.54812321), None, price("nike140620"), 1.122814),
    "nike140621" -> Transaction(
      userA, price("nike140621").date, SaleForRegularPayment, None, Some(2.01417), price("nike140621"), 0.649),
    "nike140625" -> Transaction(
      userA, price("nike140625").date, InvestmentRegular, Some(10.2), None, price("nike140625"), 3.322)
  )

  val txUserB: Map[String, Transaction] = Map(
    "nike140622" -> Transaction(
      userB, price("nike140622").date, InvestmentRegular, Some(9.12), None, price("nike140622"), 3.03),
    "nike140627" -> Transaction(
      userB, price("nike140627").date, InvestmentRegular, Some(10.2), None, price("nike140627"), 3.1)
  )
}
