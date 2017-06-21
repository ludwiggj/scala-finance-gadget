package models.org.ludwiggj.finance.persistence.file

import models.org.ludwiggj.finance.data.{TestHoldings, TestPrices, TestTransactionsMultipleFunds, userA}
import org.scalatest.{FunSuite, Matchers}

class RoundTripTest extends FunSuite with Matchers {

  val TEST_RESOURCES_DIR = "test/resources"

  test("transactions can be persisted and reconstituted") {
    new TestTransactionsMultipleFunds {
      val txFile = s"${TEST_RESOURCES_DIR}/tx_round_trip_test.txt"

      println(s"About to persist transactions: $txsMultipleFunds")

      FilePersister(txFile).write(txsMultipleFunds)

      val reconstitutedTransactions =
        FileTransactionFactory(userA, txFile).getTransactions()

      println(s"reconstitutedTransactions: $reconstitutedTransactions")

      reconstitutedTransactions should contain theSameElementsAs txsMultipleFunds
    }
  }

  test("holdings can be persisted and reconstituted") {
    new TestHoldings {
      val holdingFile = s"${TEST_RESOURCES_DIR}/holdings_round_trip_test.txt"

      println(s"About to persist transactions: $holdingsMultipleFunds")

      FilePersister(holdingFile).write(holdingsMultipleFunds)

      val reconstitutedHoldings = FileHoldingFactory(userA, holdingFile).getHoldings()

      println(s"reconstitutedHoldings: $reconstitutedHoldings")

      reconstitutedHoldings should contain theSameElementsAs holdingsMultipleFunds
    }
  }

  test("prices can be persisted and reconstituted") {
    new TestPrices {
      val priceFile = s"${TEST_RESOURCES_DIR}/price_round_trip_test.txt"

      println(s"About to persist prices: $pricesMultipleFunds")

      FilePersister(priceFile).write(pricesMultipleFunds)

      val reconstitutedPrices = FilePriceFactory(priceFile).getPrices()

      println(s"reconstitutedPrices: $reconstitutedPrices")

      reconstitutedPrices should contain theSameElementsAs pricesMultipleFunds
    }
  }
}