import org.ludwiggj.finance.{FinanceDate, Transaction}
import org.scalatest.{Matchers, FunSuite}

class TransactionSuite extends FunSuite with Matchers {

  val tx1 = Transaction(
    "BT", FinanceDate("29/04/14"), "A transaction", None, Some(BigDecimal(222)),
    FinanceDate("29/04/14"), BigDecimal(123.4), BigDecimal(23.8)
  )

  val tx2 = Transaction(
    "BT", FinanceDate("29/04/14"), "A transaction", None, Some(BigDecimal(222)),
    FinanceDate("29/04/14"), BigDecimal(123.4), BigDecimal(23.8)
  )

  val tx3 = Transaction(
    "BT", FinanceDate("29/04/14"), "A transaction", None, Some(BigDecimal(222)),
    FinanceDate("29/04/14"), BigDecimal(123.41), BigDecimal(23.8)
  )

  test("A transaction should equal itself") {
    assert(tx1 == tx1)
  }

  test("Two transactions with identical fields should be equal") {
    assert(tx1 == tx2)
  }

  test("Two transactions with different fields should not be equal") {
    assert(tx1 != tx3)
  }

  test("List comparison: order is important") {
    assert(List(tx1, tx3) !== List(tx3, tx2))
  }

  test("Set comparison: order is not important") {
    assert(List(tx1, tx3).toSet == List(tx3, tx2).toSet)
  }

  test("Set comparison: should form") {
    List(tx1, tx3).toSet should equal(List(tx3, tx2).toSet)
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
    List(tx1, tx3) should contain theSameElementsAs List(tx3, tx2)
  }

  test("not theSameElementsAs") {
    List(tx1, tx3) should not(contain theSameElementsAs List(tx1, tx2))
  }

  test("diff take 3") {
    List(tx1, tx3) diff List(tx3) should equal(List(tx1))
  }
}