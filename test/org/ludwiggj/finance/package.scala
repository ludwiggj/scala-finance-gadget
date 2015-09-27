package org.ludwiggj

import models.org.ludwiggj.finance.domain._

package object finance {

  trait TestTransactions {
    val userNameGraeme = "Graeme"

    val tx1 = Transaction(userNameGraeme,
      FinanceDate("02/05/2014"), "Dividend Reinvestment", Some(BigDecimal(0.27)), None,
      Price("Aberdeen Ethical World Equity A Fund Inc", "02/05/2014", "1.4123"), BigDecimal(0.1912)
    )

    val tx2 = Transaction(userNameGraeme,
      FinanceDate("02/05/2014"), "Dividend Reinvestment", Some(BigDecimal(5.06)), None,
      Price("Ecclesiastical Amity European A Fund Inc", "02/05/2014", "2.0290"), BigDecimal(2.4939)
    )

    val tx3 = Transaction(userNameGraeme,
      FinanceDate("02/05/2014"), "Dividend Reinvestment", Some(BigDecimal(17.39)), None,
      Price("F&C Stewardship Income 1 Fund Inc", "02/05/2014", "1.3250"), BigDecimal(13.1246)
    )

    val tx4 = Transaction(userNameGraeme,
      FinanceDate("25/04/2014"), "Investment Regular", Some(BigDecimal(200.00)), None,
      Price("M&G Feeder of Property Portfolio I Fund Acc", "25/04/2014", "11.5308"), BigDecimal(17.3449)
    )

    val transactions = List(tx1, tx2, tx3, tx4)
  }

  trait TestHoldings {
    val userNameGraeme = "Graeme"

    val holding1 = Holding(userNameGraeme, Price("Aberdeen Ethical World Equity A Fund Inc", "20/05/2014", "1.4360"),
      BigDecimal(1887.9336))

    val holding2 = Holding(userNameGraeme, Price("Henderson Global Care UK Income A Fund Inc", "20/05/2014", "1.1620"),
      BigDecimal(3564.2985))

    val holding3 = Holding(userNameGraeme, Price("Schroder Gbl Property Income Maximiser Z Fund Inc", "20/05/2014", "0.4808"),
      BigDecimal(5498.5076))

    val holdings = List(holding1, holding2, holding3)

    val holding4 = Holding(userNameGraeme, Price(" ^Schroder Gbl Property Income Maximiser Z Fund Inc ", "20/05/2014", "0.4808"),
          BigDecimal(5498.5076))
  }

  trait TestPrices {
    val price1 = Price("Henderson Global Care UK Income A Fund Inc", "25/04/2010", "0.8199")
    val price2 = Price("Schroder Gbl Property Income Maximiser Z Fund Inc", "25/12/2014", "0.4799")

    val prices = List(price1, price2)

    val priceBeforeNameCorrection = Price("^Schroder Gbl Property Income Maximiser Z Fund Inc", "25/12/2014", "0.4799")
    val priceAfterNameCorrection = Price("Schroder Gbl Property Income Maximiser Z Fund Inc", "25/12/2014", "0.4799")
  }

  trait TestFundNames {
    val fundNameBeforeNameCorrection = FundName(" ^Schroder Gbl Property Income Maximiser Z Fund Inc ")
    val fundNameAfterNameCorrection = FundName("Schroder Gbl Property Income Maximiser Z Fund Inc")
  }
}