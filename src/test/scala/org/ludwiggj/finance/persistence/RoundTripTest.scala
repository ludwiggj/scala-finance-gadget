package org.ludwiggj.finance.persistence

import org.scalatest.{Matchers, FunSuite}
import org.ludwiggj.finance._

class RoundTripTest extends FunSuite with Matchers {

  test("transactions can be persisted and reconstituted") {
    new TestTransactions {
      val txFile = "tx_round_trip_test.txt"

      println(s"About to persist transactions: $transactions")

      Persister(txFile).write(transactions)

      val reconstitutedTransactions = new FileTransactionFactory(txFile).getTransactions()

      println(s"reconstitutedTransactions: $reconstitutedTransactions")

      reconstitutedTransactions should contain theSameElementsAs transactions
    }
  }

  test("holdings can be persisted and reconstituted") {
    new TestHoldings {
      val holdingFile = "holdings_round_trip_test.txt"

      println(s"About to persist transactions: $holdings")

      Persister(holdingFile).write(holdings)

      val reconstitutedHoldings = new FileHoldingFactory(holdingFile).getHoldings()

      println(s"reconstitutedHoldings: $reconstitutedHoldings")

      reconstitutedHoldings should contain theSameElementsAs holdings
    }
  }
}