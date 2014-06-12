package org.ludwiggj.finance.persistence

import org.scalatest.{Matchers, FunSuite}
import scala.language.postfixOps
import scala.Some
import org.ludwiggj.finance.domain.{Transaction, FinanceDate}

class FileTransactionFactoryTest extends FunSuite with Matchers {

  test("Retrieve holdings from file") {

    val tx1 = Transaction(
      "F&C Stewardship Income 1 Fund Inc", FinanceDate("11/04/2014"), "Sale for Regular Payment",
      None, Some(BigDecimal(15.56)), FinanceDate("11/04/2014"), BigDecimal(130.6), BigDecimal(11.9098)
    )

    val tx2 = Transaction(
      "Schroder Gbl Property Income Maximiser Z Fund Inc", FinanceDate("31/03/2014"), "Dividend Reinvestment",
      Some(BigDecimal(30.50)), None, FinanceDate("31/03/2014"), BigDecimal(47.73), BigDecimal(63.9012)
    )

    val tx3 = Transaction(
      "CIS Sustainable Leaders C Trust Inc", FinanceDate("25/03/2014"), "Investment Regular",
      Some(BigDecimal(325.62)), None, FinanceDate("25/03/2014"), BigDecimal(128.80), BigDecimal(252.8106)
    )

    val tx4 = Transaction(
      "Ecclesiastical Amity European B Fund Inc", FinanceDate("25/03/2014"), "Investment Regular",
      Some(BigDecimal(325.62)), None, FinanceDate("25/03/2014"), BigDecimal(203.90), BigDecimal(159.6960)
    )

    val expectedTransactions = List(tx1, tx2, tx3, tx4)

    val transactionFactory = new FileTransactionFactory("fileTransactions.txt")

    val transactions = transactionFactory.getTransactions()

    println(transactions)

    transactions should contain theSameElementsAs expectedTransactions
  }
}