package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.data.{TestTransactionsMultipleFunds, userA}
import org.scalatest.{FunSuite, Matchers}

class TransactionSuite extends FunSuite with Matchers with TestTransactionsMultipleFunds {

  test("A transaction should equal itself") {
    tx("mgFeeder140425") should equal(tx("mgFeeder140425"))
  }

  test("Two transactions with identical fields should be equal") {
    tx("mgFeeder140425") should equal(tx("mgFeeder140425").copy())
  }

  test("Two transactions with different fields should not be equal") {
    tx("mgFeeder140425") should not equal (tx("ecclesiastical140502"))
  }

  test("Transaction.fundName") {
    tx("mgFeeder140425").fundName should equal(price("mgFeeder140425").fundName)
  }

  test("Transaction.priceDate") {
    tx("mgFeeder140425").priceDate should equal(price("mgFeeder140425").date)
  }

  test("Transaction.priceInPounds") {
    tx("mgFeeder140425").priceInPounds should equal(price("mgFeeder140425").inPounds)
  }

  test("Transaction.toFileFormat") {
    tx("mgFeeder140425").toFileFormat should equal(
      "M&G Feeder of Property Portfolio I Fund Acc|25/04/2014|Investment Regular|200.0||25/04/2014|11.5308|17.3449"
    )
  }

  test("Transaction.toString") {
    tx("mgFeeder140425").toString should equal(
      s"Tx [userName: $userA, holding: M&G Feeder of Property Portfolio I Fund Acc, date: 25/04/2014, "
        + "description: Investment Regular, in: Some(200.0), out: None, price date: 25/04/2014, price: 11.5308, "
        + "units: 17.3449]"
    )
  }
}