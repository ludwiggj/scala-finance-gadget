package org.ludwiggj

import org.ludwiggj.finance.domain.{Price, Holding, FinanceDate, Transaction}

package object finance {

  trait TestTransactions {
    val tx1 = Transaction(
      "Aberdeen Ethical World Equity A Fund Inc", FinanceDate("02/05/2014"), "Dividend Reinvestment",
      Some(BigDecimal(0.27)), None, FinanceDate("02/05/2014"), BigDecimal(1.4123), BigDecimal(0.1912)
    )

    val tx2 = Transaction(
      "Ecclesiastical Amity European A Fund Inc", FinanceDate("02/05/2014"), "Dividend Reinvestment",
      Some(BigDecimal(5.06)), None, FinanceDate("02/05/2014"), BigDecimal(2.0290), BigDecimal(2.4939)
    )

    val tx3 = Transaction(
      "F&C Stewardship Income 1 Fund Inc", FinanceDate("02/05/2014"), "Dividend Reinvestment",
      Some(BigDecimal(17.39)), None, FinanceDate("02/05/2014"), BigDecimal(1.3250), BigDecimal(13.1246)
    )

    val tx4 = Transaction(
      "M&G Feeder of Property Portfolio I Fund Acc", FinanceDate("25/04/2014"), "Investment Regular",
      Some(BigDecimal(200.00)), None, FinanceDate("25/04/2014"), BigDecimal(11.5308), BigDecimal(17.3449)
    )

    val transactions = List(tx1, tx2, tx3, tx4)
  }

  trait TestHoldings {
    val holding1 = Holding("Aberdeen Ethical World Equity A Fund Inc", BigDecimal(1887.9336),
      FinanceDate("20/05/2014"), BigDecimal(1.4360))

    val holding2 = Holding("Henderson Global Care UK Income A Fund Inc", BigDecimal(3564.2985),
      FinanceDate("20/05/2014"), BigDecimal(1.1620))

    val holding3 = Holding("Schroder Gbl Property Income Maximiser Z Fund Inc", BigDecimal(5498.5076),
      FinanceDate("20/05/2014"), BigDecimal(0.4808))

    val holdings = List(holding1, holding2, holding3)
  }

  trait TestPrices {
    val price1 = Price("Henderson Global Care UK Income A Fund Inc", FinanceDate("25/04/2010"), BigDecimal(0.8199))
    val price2 = Price("Schroder Gbl Property Income Maximiser Z Fund Inc", FinanceDate("25/12/2014"), BigDecimal(0.4799))

    val prices = List(price1, price2)
  }
}