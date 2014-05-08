package org.ludwiggj.finance

import org.filippodeluca.ssoup.SSoup._
import scala.io.Source
import java.io.PrintWriter

// This is a test

object FinanceTransactionScraper extends App {
  private val headerRow = 1

  // Retrieve from web site
  //  val page = loginAndParse("transactions")

  // Load from file...
//    val page = parsePageFromFile("resources/transactions.txt")
//
//    val txRows = page.select(s"table[id~=dgTransactions] tr") drop headerRow
//
//    println("Number of rows = " + txRows.size)
//
//    val txs = for (txRow <- txRows) yield Transaction(txRow.toString)

  // Get rows from page
//    val txRows = page.select(s"table[id~=dgTransactions] tr") drop headerRow

//    println("Number of rows = " + txRows.size)

//    val txs = for (txRow <- txRows) yield Transaction(txRow.toString)

  // Load from file II...

  val source = Source.fromFile("resources/transactionsOut.txt")
  val lineIterator = source.getLines
  val tokenisedLines = for (l <- lineIterator) yield l.split(Transaction.separator)
  val txs = (for (line <- tokenisedLines) yield Transaction(line)).toList

  source.close

  println(s"${txs.size} rows")
  txs.foreach(t => println(t))
  println(txs.toList.groupBy(_.description))

  // Output to file
//    val out = new PrintWriter("resources/tx140427_A.txt")
//    for (tx <- txs) out.println(tx.toFileFormat)
//    out.close
}