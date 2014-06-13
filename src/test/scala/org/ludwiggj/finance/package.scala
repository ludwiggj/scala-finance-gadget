package org.ludwiggj

import org.ludwiggj.finance.domain.{Holding, FinanceDate, Transaction}

package object finance {
  val reportHome = "reports"
  val testHome = "target/scala-2.10/test-classes"

  trait TestTransactions {
    val tx1 = Transaction(
      "Aberdeen Ethical World Equity A Fund Inc", FinanceDate("02/05/2014"), "Dividend Reinvestment",
      Some(BigDecimal(0.27)), None, FinanceDate("02/05/2014"), BigDecimal(141.23), BigDecimal(0.1912)
    )

    val tx2 = Transaction(
      "Ecclesiastical Amity European A Fund Inc", FinanceDate("02/05/2014"), "Dividend Reinvestment",
      Some(BigDecimal(5.06)), None, FinanceDate("02/05/2014"), BigDecimal(202.90), BigDecimal(2.4939)
    )

    val tx3 = Transaction(
      "F&C Stewardship Income 1 Fund Inc", FinanceDate("02/05/2014"), "Dividend Reinvestment",
      Some(BigDecimal(17.39)), None, FinanceDate("02/05/2014"), BigDecimal(132.50), BigDecimal(13.1246)
    )

    val tx4 = Transaction(
      "M&G Feeder of Property Portfolio I Fund Acc", FinanceDate("25/04/2014"), "Investment Regular",
      Some(BigDecimal(200.00)), None, FinanceDate("25/04/2014"), BigDecimal(1153.08), BigDecimal(17.3449)
    )

    val transactions = List(tx1, tx2, tx3, tx4)
  }

  trait TestHoldings {
    val holding1 = Holding("Aberdeen Ethical World Equity A Fund Inc", BigDecimal(1887.9336),
      FinanceDate("20/05/2014"), BigDecimal(143.60))

    val holding2 = Holding("Henderson Global Care UK Income A Fund Inc", BigDecimal(3564.2985),
      FinanceDate("20/05/2014"), BigDecimal(116.20))

    val holding3 = Holding("Schroder Gbl Property Income Maximiser Z Fund Inc", BigDecimal(5498.5076),
      FinanceDate("20/05/2014"), BigDecimal(48.08))

    val holdings = List(holding1, holding2, holding3)
  }
}