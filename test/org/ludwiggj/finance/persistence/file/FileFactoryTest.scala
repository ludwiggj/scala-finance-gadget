package org.ludwiggj.finance.persistence.file

import models.org.ludwiggj.finance.persistence.file.{FileHoldingFactory, FilePriceFactory, FileTransactionFactory}
import org.ludwiggj.finance.{TestHoldings, TestPrices, TestTransactions}
import org.scalatest.{FunSuite, Matchers}

import scala.language.postfixOps

class FileFactoryTest extends FunSuite with Matchers {

  test("Retrieve transactions from file") {
    new TestTransactions {

      val actualTransactions = new FileTransactionFactory("/fileTransactions.txt").getTransactions()

      actualTransactions should contain theSameElementsAs transactions
    }
  }

  test("First transaction from file toString") {
    new TestTransactions {

      val actualTransactions = new FileTransactionFactory("/fileTransactions.txt").getTransactions()

      (actualTransactions head).toString() should equal(
        "Tx [holding: Aberdeen Ethical World Equity A Fund Inc, date: 02/05/2014, description: Dividend Reinvestment, " +
          "in: Some(0.27), out: None, price date: 02/05/2014, price: 1.4123, units: 0.1912]"
      )
    }
  }

  test("Retrieve holdings from file") {
    new TestHoldings {

      val actualHoldings = new FileHoldingFactory("/fileHoldings.txt").getHoldings()

      actualHoldings should contain theSameElementsAs holdings
    }
  }

  test("First holding from file toString") {
    new TestHoldings {

      val actualHoldings = new FileHoldingFactory("/fileHoldings.txt").getHoldings()

      (actualHoldings head).toString() should equal(
        "Financial Holding [name: Aberdeen Ethical World Equity A Fund Inc, units: 1887.9336, "
          + "date: 20/05/2014, price: £1.436, value: £2711.07]"
      )
    }
  }

  test("Retrieve prices from file") {
    new TestPrices {

      val actualPrices = new FilePriceFactory("/filePrices.txt").getPrices()

      actualPrices should contain theSameElementsAs prices
    }
  }

  test("First price from file toString") {
      new TestPrices {

        val actualPrices = new FilePriceFactory("/filePrices.txt").getPrices()

        (actualPrices head).toString() should equal(
          "Price [name: Henderson Global Care UK Income A Fund Inc, date: 25/04/2010, price: £0.8199]"
        )
      }
    }
}