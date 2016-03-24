package models.org.ludwiggj.finance

import domain._
import Transaction._

package object data {
  val userNameGraeme = "Graeme"

  trait TestTransactions {
    val userNameGraeme = "Graeme"

    val tx1 = Transaction(userNameGraeme,
      "02/05/2014", DividendReinvestment, Some(0.27), None,
      Price("Aberdeen Ethical World Equity A Fund Inc", "02/05/2014", "1.4123"), 0.1912
    )

    val tx2 = Transaction(userNameGraeme,
      "02/05/2014", DividendReinvestment, Some(5.06), None,
      Price("Ecclesiastical Amity European A Fund Inc", "02/05/2014", "2.0290"), 2.4939
    )

    val tx3 = Transaction(userNameGraeme,
      "02/05/2014", DividendReinvestment, Some(17.39), None,
      Price("F&C Stewardship Income 1 Fund Inc", "02/05/2014", "1.3250"), 13.1246
    )

    val tx4 = Transaction(userNameGraeme,
      "25/04/2014", InvestmentRegular, Some(200.00), None,
      Price("M&G Feeder of Property Portfolio I Fund Acc", "25/04/2014", "11.5308"), 17.3449
    )

    val tx5 = Transaction(userNameGraeme,
      "06/11/2015", UnitShareConversionOut, None, None,
      Price("Aberdeen Ethical World Equity A Fund Inc", "04/11/2015", "0"), 22.7723
    )

    val transactionsMultipleFunds = List(tx1, tx2, tx3, tx4, tx5)
  }

  trait TestHoldings {
    val userNameGraeme = "Graeme"

    val holding1 = Holding(userNameGraeme,
      Price("Aberdeen Ethical World Equity A Fund Inc", "20/05/2014", "1.4360"), 1887.9336)

    val holding2 = Holding(userNameGraeme,
      Price("Henderson Global Care UK Income A Fund Inc", "20/05/2014", "1.1620"), 3564.2985)

    val holding3 = Holding(userNameGraeme,
      Price("Schroder Gbl Property Income Maximiser Z Fund Inc", "20/05/2014", "0.4808"), 5498.5076)

    val holdingsMultipleFunds = List(holding1, holding2, holding3)

    val holding4 = Holding(userNameGraeme,
      Price(" ^Schroder Gbl Property Income Maximiser Z Fund Inc ", "20/05/2014", "0.4808"), 5498.5076)
  }

  trait TestPrices {
    val price1 = Price("Henderson Global Care UK Income A Fund Inc", "25/04/2010", "0.8199")
    val price2 = Price("Schroder Gbl Property Income Maximiser Z Fund Inc", "25/12/2014", "0.4799")

    val pricesMultipleFunds = List(price1, price2)

    val priceBeforeNameCorrection = Price("^Schroder Gbl Property Income Maximiser Z Fund Inc", "25/12/2014", "0.4799")
    val priceAfterNameCorrection = Price("Schroder Gbl Property Income Maximiser Z Fund Inc", "25/12/2014", "0.4799")
  }

  trait TestFundNames {
    val fundNameBeforeNameCorrection = FundName(" ^Schroder Gbl Property Income Maximiser Z Fund Inc ")
    val fundNameAfterNameCorrection = FundName("Schroder Gbl Property Income Maximiser Z Fund Inc")
  }

  trait TestTransactionsAndPrice {
    val aberdeenEthicalFundName = "Aberdeen Ethical World Equity A Fund Inc"
    val aberdeenEthicalPrice140625 = Price(aberdeenEthicalFundName, "25/06/2014", "1.4123")
    val aberdeenEthicalLatestPrice = Price(aberdeenEthicalFundName, "01/10/2014", "1.8199")

    private val aberdeenEthicalTx1 = Transaction(userNameGraeme, "25/06/2014", InvestmentRegular, Some(200.00), None,
      aberdeenEthicalPrice140625, 141.6130
    )

    private val aberdeenEthicalTx2 = Transaction(userNameGraeme, "25/05/2014", InvestmentLumpSum, Some(100.00), None,
      Price(aberdeenEthicalFundName, "25/05/2014", "2.0290"), 49.2854
    )

    private val aberdeenEthicalTx3 = Transaction(userNameGraeme, "25/04/2014", InvestmentRegular, Some(150.50), None,
      Price(aberdeenEthicalFundName, "25/04/2014", "1.7209"), 87.4542
    )

    private val aberdeenEthicalTx4 = Transaction(userNameGraeme, "02/03/2014", DividendReinvestment, Some(5.06), None,
      Price(aberdeenEthicalFundName, "02/03/2014", "1.8381"), 2.7528
    )

    private val aberdeenEthicalTx5 = Transaction(userNameGraeme, "01/03/2014", SaleForRegularPayment, None, Some(13.75),
      Price(aberdeenEthicalFundName, "01/03/2014", "1.5641"), 8.7910
    )

    val aberdeenEthicalTransactions = List(
      aberdeenEthicalTx1, aberdeenEthicalTx2, aberdeenEthicalTx3, aberdeenEthicalTx4, aberdeenEthicalTx5
    )

    val aberdeenEthicalConversionOut = Transaction(userNameGraeme, "01/10/2014", UnitShareConversionOut, None, None,
      aberdeenEthicalLatestPrice, 272.3144
    )

    val aberdeenEthicalTransactionsWithConversionOut = aberdeenEthicalConversionOut :: aberdeenEthicalTransactions

    val aberdeenEthicalConversionIn = Transaction(userNameGraeme, "01/02/2014", "Unit/Share Conversion +", None, None,
      Price(aberdeenEthicalFundName, "01/02/2014", 1.9543), 1234.4321)

    val aberdeenEthicalTransactionsWithConversionIn = aberdeenEthicalConversionIn :: aberdeenEthicalTransactions

    val newAberdeenEthicalFundName = "Aberdeen Ethical World Equity B Fund Inc"

    val newAberdeenEthicalPrice140901 = Price(newAberdeenEthicalFundName, "01/09/2014", "15.2275")

    private val newAberdeenEthicalTx1 = Transaction(userNameGraeme, "01/09/2014", InvestmentRegular, Some(750.00), None,
      newAberdeenEthicalPrice140901, 45.6974
    )

    private val newAberdeenEthicalTx2 = Transaction(userNameGraeme, "01/08/2014", InvestmentLumpSum, Some(50.00), None,
      Price(newAberdeenEthicalFundName, "01/08/2014", "15.6950"), 3.1857
    )

    val newAberdeenEthicalTransactions = List(newAberdeenEthicalTx1, newAberdeenEthicalTx2)
  }

  trait TestHoldingSummary extends TestTransactionsAndPrice {

    val aberdeenEthicalHoldingSummary = HoldingSummary(aberdeenEthicalTransactions, aberdeenEthicalPrice140625)

    val aberdeenEthicalHoldingSummaryWithConversionOut =
      HoldingSummary(aberdeenEthicalTransactionsWithConversionOut, aberdeenEthicalLatestPrice)

    val aberdeenEthicalHoldingSummaryWithConversionIn =
      HoldingSummary(aberdeenEthicalTransactionsWithConversionIn, aberdeenEthicalPrice140625)

    val newAberdeenEthicalHoldingSummary = HoldingSummary(newAberdeenEthicalTransactions, newAberdeenEthicalPrice140901)
  }

  trait TestHoldingSummaries extends TestTransactionsAndPrice {

    val newAberdeenEthicalLatestPrice = Price(newAberdeenEthicalFundName, "01/10/2014", "17.2081")

    val newAberdeenEthicalConversionIn = Transaction(userNameGraeme, "01/10/2014", UnitShareConversionIn, None,
      None, newAberdeenEthicalLatestPrice, 24.9576
    )

    val aberdeenEthicalWithConversionsTransactionMap =
      Map(
        (userNameGraeme, aberdeenEthicalFundName) ->
          (aberdeenEthicalTransactionsWithConversionOut, aberdeenEthicalLatestPrice),
        (userNameGraeme, newAberdeenEthicalFundName) ->
          (newAberdeenEthicalConversionIn :: newAberdeenEthicalTransactions, newAberdeenEthicalLatestPrice)
      )
  }
}