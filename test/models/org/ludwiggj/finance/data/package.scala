package models.org.ludwiggj.finance

import domain._
import persistence.database.TransactionsDatabase._

package object data {

  trait TestTransactions {
    val userNameGraeme = "Graeme"

    val tx1 = Transaction(userNameGraeme,
      "02/05/2014", "Dividend Reinvestment", Some(0.27), None,
      Price("Aberdeen Ethical World Equity A Fund Inc", "02/05/2014", "1.4123"), 0.1912
    )

    val tx2 = Transaction(userNameGraeme,
      "02/05/2014", "Dividend Reinvestment", Some(5.06), None,
      Price("Ecclesiastical Amity European A Fund Inc", "02/05/2014", "2.0290"), 2.4939
    )

    val tx3 = Transaction(userNameGraeme,
      "02/05/2014", "Dividend Reinvestment", Some(17.39), None,
      Price("F&C Stewardship Income 1 Fund Inc", "02/05/2014", "1.3250"), 13.1246
    )

    val tx4 = Transaction(userNameGraeme,
      "25/04/2014", InvestmentRegular, Some(200.00), None,
      Price("M&G Feeder of Property Portfolio I Fund Acc", "25/04/2014", "11.5308"), 17.3449
    )

    val transactions = List(tx1, tx2, tx3, tx4)
  }

  trait TestHoldings {
    val userNameGraeme = "Graeme"

    val holding1 = Holding(userNameGraeme,
      Price("Aberdeen Ethical World Equity A Fund Inc", "20/05/2014", "1.4360"), 1887.9336)

    val holding2 = Holding(userNameGraeme,
      Price("Henderson Global Care UK Income A Fund Inc", "20/05/2014", "1.1620"), 3564.2985)

    val holding3 = Holding(userNameGraeme,
      Price("Schroder Gbl Property Income Maximiser Z Fund Inc", "20/05/2014", "0.4808"), 5498.5076)

    val holdings = List(holding1, holding2, holding3)

    val holding4 = Holding(userNameGraeme,
      Price(" ^Schroder Gbl Property Income Maximiser Z Fund Inc ", "20/05/2014", "0.4808"), 5498.5076)
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

  trait TestTransactionsAndPrice {
    private val userNameGraeme = "Graeme"
    private val aberdeenEthical = "Aberdeen Ethical World Equity A Fund Inc"

    private val tx1 = Transaction(userNameGraeme,
      "25/06/2014", InvestmentRegular, Some(200.00), None,
      Price(aberdeenEthical, "25/06/2014", "1.4123"), 141.6130
    )

    private val tx2 = Transaction(userNameGraeme,
      "25/05/2014", InvestmentLumpSum, Some(100.00), None,
      Price(aberdeenEthical, "25/05/2014", "2.0290"), 49.2854
    )

    private val tx3 = Transaction(userNameGraeme,
      "25/04/2014", InvestmentRegular, Some(150.50), None,
      Price(aberdeenEthical, "25/04/2014", "1.7209"), 87.4542
    )

    private val tx4 = Transaction(userNameGraeme,
      "02/03/2014", DividendReinvestment, Some(5.06), None,
      Price(aberdeenEthical, "02/03/2014", "1.8381"), 2.7528
    )

    private val tx5 = Transaction(userNameGraeme,
      "01/03/2014", SaleForRegularPayment, None, Some(13.75),
      Price(aberdeenEthical, "01/03/2014", "1.5641"), 8.7910
    )

    val transactions = List(tx1, tx2, tx3, tx4, tx5)

    val txOut = Transaction(userNameGraeme,
      "21/07/2014", "Unit/Share Conversion -", None, None,
      Price(aberdeenEthical, "21/07/2014", 0.0), 272.3144)

    val transactionsWithConversionOut = txOut :: transactions

    val txIn = Transaction(userNameGraeme,
      "01/02/2014", "Unit/Share Conversion +", None, None,
      Price(aberdeenEthical, "01/02/2014", 0.0), 1234.4321)

    val transactionsWithConversionIn = txIn :: transactions

    val price = Price(aberdeenEthical, "05/07/2014", "1.8199")
  }
}