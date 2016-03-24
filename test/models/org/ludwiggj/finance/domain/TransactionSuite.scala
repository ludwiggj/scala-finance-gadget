package models.org.ludwiggj.finance.domain

import FinanceDate.stringToFinanceDate
import models.org.ludwiggj.finance.Transaction
import models.org.ludwiggj.finance.Transaction.InvestmentRegular
import models.org.ludwiggj.finance.data.TestTransactions
import org.scalatest.{FunSuite, Matchers}


class TransactionSuite extends FunSuite with Matchers {

  val tx6 = Transaction("Graeme",
    "25/04/2014", InvestmentRegular, Some(200.00), None,
    Price("M&G Feeder of Property Portfolio I Fund Acc", "25/04/2014", "11.5308"), 17.3449
  )

  val tx7 = Transaction("Graeme",
    FinanceDate("29/04/2014"), InvestmentRegular, None, Some(222), Price("BT", "29/04/2014", "1.2341"), 23.8
  )

  test("A transaction should equal itself") {
    new TestTransactions {
      tx4 should equal(tx4)
    }
  }

  test("Two transactions with identical fields should be equal") {
    new TestTransactions {
      tx4 should equal(tx6)
    }
  }

  test("Two transactions with different fields should not be equal") {
    new TestTransactions {
      tx4 should not equal (tx7)
    }
  }

  test("List comparison: order is important") {
    new TestTransactions {
      List(tx4, tx7) should not equal (List(tx7, tx6))
    }
  }

  test("Set comparison: order is not important") {
    new TestTransactions {
      List(tx4, tx7).toSet should equal(List(tx7, tx6).toSet)
    }
  }

  test("Set comparison: should form") {
    new TestTransactions {
      List(tx4, tx7).toSet should equal(List(tx7, tx6).toSet)
    }
  }

  test("filterNot take 1") {
    List(1, 2, 3, 4, 5) filterNot List(1, 2, 3).contains should equal(List(4, 5))
  }

  test("filterNot take 2") {
    List(1, 2, 3, 4, 5, 1) filterNot List(1, 2, 3).contains should equal(List(4, 5))
  }

  test("diff take 1") {
    List(1, 2, 3, 4, 5) diff List(1, 2, 3) should equal(List(4, 5))
  }

  test("diff take 2") {
    List(1, 2, 3, 4, 5, 1) diff List(1, 2, 3) should equal(List(4, 5, 1))
  }

  test("theSameElementsAs") {
    new TestTransactions {
      List(tx4, tx7) should contain theSameElementsAs List(tx7, tx6)
    }
  }

  test("not theSameElementsAs") {
    new TestTransactions {
      List(tx4, tx7) should not(contain theSameElementsAs List(tx4, tx6))
    }
  }

  test("diff take 3") {
    new TestTransactions {
      List(tx4, tx7) diff List(tx7) should equal(List(tx4))
    }
  }

  test("Transaction.fundName is correct") {
    new TestTransactions {
      tx1.fundName should equal(FundName("Aberdeen Ethical World Equity A Fund Inc"))
    }
  }

  test("Transaction.date is correct") {
    new TestTransactions {
      tx1.date should equal(FinanceDate("02/05/2014"))
    }
  }

  test("Transaction.description is correct") {
    new TestTransactions {
      tx1.description should equal("Dividend Reinvestment")
    }
  }

  test("Transaction.in is correct") {
    new TestTransactions {
      tx1.in should equal(Some(0.27))
    }
  }

  test("Transaction.out is correct") {
    new TestTransactions {
      tx1.out should equal(None)
    }
  }

  test("Transaction.priceDate is correct") {
    new TestTransactions {
      tx1.priceDate should equal(FinanceDate("02/05/2014"))
    }
  }

  test("Transaction.priceInPounds is correct") {
    new TestTransactions {
      tx1.priceInPounds should equal(1.4123)
    }
  }

  test("Transaction.units is correct") {
    new TestTransactions {
      tx1.units should equal(0.1912)
    }
  }

  test("Transaction.toFileFormat is correct") {
    new TestTransactions {
      tx1.toFileFormat should equal(
        "Aberdeen Ethical World Equity A Fund Inc|02/05/2014|Dividend Reinvestment|0.27||02/05/2014|1.4123|0.1912"
      )
    }
  }

  test("Transaction.toString is correct") {
    new TestTransactions {
      tx1.toString should equal(
        s"Tx [userName: $userNameGraeme, holding: Aberdeen Ethical World Equity A Fund Inc, date: 02/05/2014, "
          + "description: Dividend Reinvestment, in: Some(0.27), out: None, price date: 02/05/2014, price: 1.4123, "
          + "units: 0.1912]"
      )
    }
  }
}