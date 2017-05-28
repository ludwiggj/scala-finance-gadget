package models.org.ludwiggj.finance

import domain._
import Transaction._
import models.org.ludwiggj.finance.persistence.database.TransactionMap

package object data {
  val userA = "User A"

  trait TestPrices {

    val price: Map[String, Price] = Map(
      "mgFeeder140425" -> Price("M&G Feeder of Property Portfolio I Fund Acc", "25/04/2014", 11.5308),
      "aberdeen140502" -> Price("Aberdeen Ethical World Equity A Fund Inc", "02/05/2014", 1.4123),
      "ecclesiastical140502" -> Price("Ecclesiastical Amity European A Fund Inc", "02/05/2014", 2.0290),
      "fcStewardship140502" -> Price("F&C Stewardship Income 1 Fund Inc", "02/05/2014", 1.3250),
      "aberdeen151104" -> Price("Aberdeen Ethical World Equity A Fund Inc", "04/11/2015", 0)
    )

    val pricesMultipleFunds = List(
      price("mgFeeder140425"), price("ecclesiastical140502"), price("fcStewardship140502"), price("aberdeen151104")
    )
  }

  trait TestTransactionsMultipleFunds extends TestPrices {
    val tx: Map[String, Transaction] = Map(
      "mgFeeder140425" -> Transaction(
        userA, price("mgFeeder140425").date, InvestmentRegular, Some(200.00), None, price("mgFeeder140425"), 17.3449),
      "ecclesiastical140502" -> Transaction(
        userA, price("ecclesiastical140502").date, DividendReinvestment, Some(5.06), None, price("ecclesiastical140502"), 2.4939),
      "fcStewardship140502" -> Transaction(
        userA, price("fcStewardship140502").date, DividendReinvestment, Some(17.39), None, price("fcStewardship140502"), 13.1246),
      "aberdeenConversionOut151106" -> Transaction(
        userA, "06/11/2015", UnitShareConversionOut, None, None, price("aberdeen151104"), 22.7723)
    )

    val txsMultipleFunds = List(
      tx("mgFeeder140425"), tx("ecclesiastical140502"), tx("fcStewardship140502"), tx("aberdeenConversionOut151106")
    )
  }

  trait TestTransactions {
    val price: Map[String, Price] = Map(
      "aberdeen140201" -> Price("Aberdeen Ethical World Equity A Fund Inc", "01/02/2014", 1.9543),
      "aberdeen140301" -> Price("Aberdeen Ethical World Equity A Fund Inc", "01/03/2014", 1.5641),
      "aberdeen140302" -> Price("Aberdeen Ethical World Equity A Fund Inc", "02/03/2014", 1.8381),
      "aberdeen140425" -> Price("Aberdeen Ethical World Equity A Fund Inc", "25/04/2014", 1.7209),
      "aberdeen140525" -> Price("Aberdeen Ethical World Equity A Fund Inc", "25/05/2014", 2.0290),
      "aberdeen140625" -> Price("Aberdeen Ethical World Equity A Fund Inc", "25/06/2014", 1.4123),
      "aberdeen141001" -> Price("Aberdeen Ethical World Equity A Fund Inc", "01/10/2014", 1.8199),
      "aberdeenB140801" -> Price("Aberdeen Ethical World Equity B Fund Inc", "01/08/2014", 15.6950),
      "aberdeenB140901" -> Price("Aberdeen Ethical World Equity B Fund Inc", "01/09/2014", 15.2275),
      "aberdeenB141001" -> Price("Aberdeen Ethical World Equity B Fund Inc", "01/10/2014", 17.2081)
    )

    val tx: Map[String, Transaction] = Map(
      "aberdeen140301" -> Transaction(
        userA, price("aberdeen140301").date, SaleForRegularPayment, None, Some(13.75), price("aberdeen140301"), 8.7910),
      "aberdeen140302" -> Transaction(
        userA, price("aberdeen140302").date, DividendReinvestment, Some(5.06), None, price("aberdeen140302"), 2.7528),
      "aberdeen140425" -> Transaction(
        userA, price("aberdeen140425").date, InvestmentRegular, Some(150.50), None, price("aberdeen140425"), 87.4542),
      "aberdeen140525" -> Transaction(
        userA, price("aberdeen140525").date, InvestmentLumpSum, Some(100.00), None, price("aberdeen140525"), 49.2854),
      "aberdeen140625" -> Transaction(
        userA, price("aberdeen140625").date, InvestmentRegular, Some(200.00), None, price("aberdeen140625"), 141.6130),
      "aberdeen141001" -> Transaction(
        userA, price("aberdeen141001").date, UnitShareConversionOut, None, None, price("aberdeen141001"), 272.3144),
      "aberdeenB140801" -> Transaction(
        userA, price("aberdeenB140901").date, InvestmentLumpSum, Some(50.00), None, price("aberdeenB140901"), 3.1857),
      "aberdeenB140901" -> Transaction(
        userA, price("aberdeenB140901").date, InvestmentRegular, Some(750.00), None, price("aberdeenB140901"), 45.6974),
      "aberdeenB141001" -> Transaction(
        userA, price("aberdeenB141001").date, UnitShareConversionIn, None, None, price("aberdeenB141001"), 24.9576)
    )

    val aberdeenTxs =
      List(tx("aberdeen140625"), tx("aberdeen140525"), tx("aberdeen140425"), tx("aberdeen140302"), tx("aberdeen140301"))

    val aberdeenBTxs = List(tx("aberdeenB140901"), tx("aberdeenB140801"))
  }

  trait TestHoldings {
    val price: Map[String, Price] = Map(
      "aberdeen140502" -> Price("Aberdeen Ethical World Equity A Fund Inc", "02/05/2014", 1.4123),
      "henderson140520" -> Price("Henderson Global Care UK Income A Fund Inc", "20/05/2014", 1.1620),
      "schroder140520" -> Price("Schroder Gbl Property Income Maximiser Z Fund Inc", "20/05/2014", 0.4808),
      "^schroder140520" -> Price(" ^Schroder Gbl Property Income Maximiser Z Fund Inc ", "20/05/2014", "0.4808")
    )

    val holding: Map[String, Holding] = Map(
      "aberdeen140502" -> Holding(userA, price("aberdeen140502"), 1887.9336),
      "henderson140520" -> Holding(userA, price("henderson140520"), 3564.2985),
      "schroder140520" -> Holding(userA, price("schroder140520"), 5498.5076),
      "cleanedUp" -> Holding(userA, price("^schroder140520"), 5498.5076)
    )

    val holdingsMultipleFunds = List(holding("aberdeen140502"), holding("henderson140520"), holding("schroder140520"))
  }

  trait TestHoldingSummary extends TestTransactions {
    val holdingSummary: Map[String, HoldingSummary] = Map(
      "aberdeen140625" -> HoldingSummary(aberdeenTxs, price("aberdeen140625")),
      "aberdeen141001" -> HoldingSummary(tx("aberdeen141001") :: aberdeenTxs, price("aberdeen141001")),
      "aberdeenB140901" -> HoldingSummary(aberdeenBTxs, price("aberdeenB140901")),
      "aberdeenB141001" -> HoldingSummary(tx("aberdeenB141001") :: aberdeenBTxs, price("aberdeenB141001"))
    )
  }

  trait TestHoldingSummaries extends TestTransactions {
    private val txMap: Map[String, TransactionMap] = Map(
      "aberdeen140901" -> Map(
        (userA, price("aberdeen140625").fundName.name) -> (aberdeenTxs, price("aberdeen140625")),
        (userA, price("aberdeenB140901").fundName.name) -> (aberdeenBTxs, price("aberdeenB140901"))
      ),
      "aberdeen141001" -> Map(
        (userA, price("aberdeen141001").fundName.name) -> (tx("aberdeen141001") :: aberdeenTxs, price("aberdeen141001")),
        (userA, price("aberdeenB141001").fundName.name) -> (tx("aberdeenB141001") :: aberdeenBTxs, price("aberdeenB141001"))
      )
    )

    val holdingSummaries: Map[String, HoldingSummaries] = Map(
      "aberdeen140915" -> HoldingSummaries(txMap("aberdeen140901"), userA, "15/09/2014"),
      "aberdeen140930" -> HoldingSummaries(txMap("aberdeen141001"), userA, "30/09/2014"),
      "aberdeen141001" -> HoldingSummaries(txMap("aberdeen141001"), userA, "01/10/2014")
    )
  }
}