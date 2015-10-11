package models.org.ludwiggj.finance.persistence.file

import models.org.ludwiggj.finance.data.{TestHoldings, TestTransactions}
import org.scalatest.{FunSuite, Matchers}

class RoundTripTest extends FunSuite with Matchers {

  test("transactions can be persisted and reconstituted") {
    new TestTransactions {
      val txFile = "tx_round_trip_test.txt"

      println(s"About to persist transactions: $transactions")

      FilePersister(txFile).write(transactions)

      val reconstitutedTransactions =
        FileTransactionFactory(userNameGraeme, txFile).getTransactions()

      println(s"reconstitutedTransactions: $reconstitutedTransactions")

      reconstitutedTransactions should contain theSameElementsAs transactions
    }
  }

  test("holdings can be persisted and reconstituted") {
    new TestHoldings {
      val holdingFile = "holdings_round_trip_test.txt"

      println(s"About to persist transactions: $holdings")

      FilePersister(holdingFile).write(holdings)

      val reconstitutedHoldings = FileHoldingFactory(userNameGraeme, holdingFile).getHoldings()

      println(s"reconstitutedHoldings: $reconstitutedHoldings")

      reconstitutedHoldings should contain theSameElementsAs holdings
    }
  }
}