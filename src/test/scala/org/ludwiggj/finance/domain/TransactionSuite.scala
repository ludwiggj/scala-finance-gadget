package org.ludwiggj.finance.domain

import org.scalatest.{Matchers, FunSuite}
import org.ludwiggj.finance.TestTransactions

class TransactionSuite extends FunSuite with Matchers {

  val tx5 = Transaction(
    "M&G Feeder of Property Portfolio I Fund Acc", FinanceDate("25/04/2014"), "Investment Regular",
    Some(BigDecimal(200.00)), None, FinanceDate("25/04/2014"), BigDecimal(1153.08), BigDecimal(17.3449)
  )

  val tx6 = Transaction(
    "BT", FinanceDate("29/04/2014"), "A transaction", None, Some(BigDecimal(222)),
    FinanceDate("29/04/2014"), BigDecimal(123.41), BigDecimal(23.8)
  )

  test("A transaction should equal itself") {
    new TestTransactions {
      assert(tx4 == tx4)
    }
  }

  test("Two transactions with identical fields should be equal") {
    new TestTransactions {
      assert(tx4 == tx5)
    }
  }

  test("Two transactions with different fields should not be equal") {
    new TestTransactions {
      assert(tx4 != tx6)
    }
  }

  test("List comparison: order is important") {
    new TestTransactions {
      assert(List(tx4, tx6) !== List(tx6, tx5))
    }
  }

  test("Set comparison: order is not important") {
    new TestTransactions {
      assert(List(tx4, tx6).toSet == List(tx6, tx5).toSet)
    }
  }

  test("Set comparison: should form") {
    new TestTransactions {
      List(tx4, tx6).toSet should equal(List(tx6, tx5).toSet)
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
      List(tx4, tx6) should contain theSameElementsAs List(tx6, tx5)
    }
  }

  test("not theSameElementsAs") {
    new TestTransactions {
      List(tx4, tx6) should not(contain theSameElementsAs List(tx4, tx5))
    }
  }

  test("diff take 3") {
    new TestTransactions {
      List(tx4, tx6) diff List(tx6) should equal(List(tx4))
    }
  }
}