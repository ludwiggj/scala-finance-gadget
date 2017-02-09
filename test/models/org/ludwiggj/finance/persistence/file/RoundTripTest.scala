package models.org.ludwiggj.finance.persistence.file

import models.org.ludwiggj.finance.data.{TestHoldings, TestPrices, TestTransactions}
import org.scalatest.{FunSuite, Matchers}

class RoundTripTest extends FunSuite with Matchers {

  test("transactions can be persisted and reconstituted") {
    new TestTransactions {
      val txFile = "tx_round_trip_test.txt"

      println(s"About to persist transactions: $transactionsMultipleFunds")

      FilePersister(txFile).write(transactionsMultipleFunds)

      val reconstitutedTransactions =
        FileTransactionFactory(userNameGraeme, txFile).getTransactions()

      println(s"reconstitutedTransactions: $reconstitutedTransactions")

      reconstitutedTransactions should contain theSameElementsAs transactionsMultipleFunds
    }
  }

  test("holdings can be persisted and reconstituted") {
    new TestHoldings {
      val holdingFile = "holdings_round_trip_test.txt"

      println(s"About to persist transactions: $holdingsMultipleFunds")

      FilePersister(holdingFile).write(holdingsMultipleFunds)

      val reconstitutedHoldings = FileHoldingFactory(userNameGraeme, holdingFile).getHoldings()

      println(s"reconstitutedHoldings: $reconstitutedHoldings")

      reconstitutedHoldings should contain theSameElementsAs holdingsMultipleFunds
    }
  }

  test("prices can be persisted and reconstituted") {
    new TestPrices {
      val priceFile = "price_round_trip_test.txt"

      println(s"About to persist prices: $pricesMultipleFunds")

      FilePersister(priceFile).write(pricesMultipleFunds)

      val reconstitutedPrices = FilePriceFactory(priceFile).getPrices()

      println(s"reconstitutedPrices: $reconstitutedPrices")

      reconstitutedPrices should contain theSameElementsAs pricesMultipleFunds
    }
  }
}