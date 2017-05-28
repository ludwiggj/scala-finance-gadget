package models.org.ludwiggj.finance.persistence.file

import models.org.ludwiggj.finance.data.{TestHoldings, TestPrices, TestTransactionsMultipleFunds, userA}
import org.scalatest.{FunSuite, Matchers}

import scala.language.postfixOps

class FileFactoryTest extends FunSuite with Matchers {

  test("Retrieve transactions from file") {
    new TestTransactionsMultipleFunds {

      val actualTransactions =
        FileTransactionFactory(userA, "/fileTransactions.txt").getTransactions()

      actualTransactions should contain theSameElementsAs txsMultipleFunds
    }
  }

  test("First transaction from file toString") {
    new TestTransactionsMultipleFunds {

      val actualTransactions =
        FileTransactionFactory(userA, "/fileTransactions.txt").getTransactions()

      (actualTransactions head).toString() should equal(
        s"Tx [userName: $userA, holding: M&G Feeder of Property Portfolio I Fund Acc, date: 25/04/2014, "
          + "description: Investment Regular, in: Some(200.0), out: None, price date: 25/04/2014, price: 11.5308, "
          + "units: 17.3449]"
      )
    }
  }

  test("Retrieve holdings from file") {
    new TestHoldings {

      val actualHoldings = FileHoldingFactory(userA, "/fileHoldings.txt").getHoldings()

      actualHoldings should contain theSameElementsAs holdingsMultipleFunds
    }
  }

  test("First holding from file toString") {
    new TestHoldings {

      val actualHoldings = FileHoldingFactory(userA, "/fileHoldings.txt").getHoldings()

      (actualHoldings head).toString() should equal(
        s"Financial Holding [userName: $userA, name: Aberdeen Ethical World Equity A Fund Inc, "
          + "units: 1887.9336, date: 02/05/2014, price: £1.4123, value: £2666.33]"
      )
    }
  }

  test("Retrieve prices from file") {
    new TestPrices {

      val actualPrices = FilePriceFactory("/filePrices.txt").getPrices()

      actualPrices should contain theSameElementsAs pricesMultipleFunds
    }
  }

  test("First price from file toString") {
    new TestPrices {

      val actualPrices = FilePriceFactory("/filePrices.txt").getPrices()

      (actualPrices head).toString() should equal(
        "Price [name: M&G Feeder of Property Portfolio I Fund Acc, date: 25/04/2014, price: £11.5308]"
      )
    }
  }
}