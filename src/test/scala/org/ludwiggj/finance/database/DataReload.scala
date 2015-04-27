package org.ludwiggj.finance.database

import java.io.{FilenameFilter, File}

import org.ludwiggj.finance._
import org.ludwiggj.finance.persistence.{FileTransactionFactory, DatabasePersister, FileHoldingFactory}

import scala.util.matching.Regex

object DataReload extends App {

  def isAFileMatching(pattern: Regex) = new FilenameFilter {
    override def accept(dir: File, name: String) = {
      name match {
        case pattern(userName) => true
        case _ => false
      }
    }
  }

  def reloadHoldings() = {
    val holdingPattern = """holdings.*_([a-zA-Z]+)\.txt""".r
    for (
      aFile <- new File(reportHome).listFiles(isAFileMatching(holdingPattern))
    ) {
      val fileName = aFile.getName
      val holdingPattern(userName) = fileName
      val holdings = new FileHoldingFactory(s"$reportHome/$fileName").getHoldings()

      println(s"Persisting holdings for user $userName, file $fileName")
      new DatabasePersister().persistHoldings(userName, holdings)
    }
  }

  def reloadTransactions() = {
    val transactionPattern =
      """txs.*_([a-zA-Z]+)\.txt""".r
    for (
      aFile <- new File(reportHome).listFiles(isAFileMatching(transactionPattern))
    ) {
      val fileName = aFile.getName
      val transactionPattern(userName) = fileName
      val transactions = new FileTransactionFactory(s"$reportHome/$fileName").getTransactions()

      println(s"Persisting transactions for user $userName, file $fileName")
      new DatabasePersister().persistTransactions(userName, transactions)
    }
  }

  reloadHoldings()
  reloadTransactions()
}