package org.ludwiggj.finance.persistence

import org.scalatest.{Matchers, FunSuite}
import scala.language.postfixOps
import org.ludwiggj.finance._

class FileFactoryTest extends FunSuite with Matchers {

  test("Retrieve transactions from file") {
    new TestTransactions {

      val actualTransactions = new FileTransactionFactory("fileTransactions.txt").getTransactions()

      actualTransactions should contain theSameElementsAs transactions
    }
  }

  test("Retrieve holdings from file") {
    new TestHoldings {

      val actualHoldings = new FileHoldingFactory("fileHoldings.txt").getHoldings()

      actualHoldings should contain theSameElementsAs holdings
    }
  }
}