package org.ludwiggj.finance.application

import java.io.{File, FilenameFilter}

import org.ludwiggj.finance.persistence.database.Tables._
import org.ludwiggj.finance.persistence.database.DatabasePersister
import org.ludwiggj.finance.persistence.file.{FilePriceFactory, FileHoldingFactory, FileTransactionFactory}

import scala.slick.driver.MySQLDriver.simple._
import scala.util.matching.Regex

object DataReload extends App {

  def isAFileWithUserNameMatching(pattern: Regex) = new FilenameFilter {
    override def accept(dir: File, name: String) = {
      name match {
        case pattern(userName) => true
        case _ => false
      }
    }
  }

  def isAFileWithoutUserNameMatching(pattern: Regex) = new FilenameFilter {
    override def accept(dir: File, name: String) = {
      pattern.findFirstIn(name).isDefined
    }
  }

  def deleteAllRows() = {
    val db = Database.forConfig("db")
    val users: TableQuery[Users] = TableQuery[Users]
    val funds: TableQuery[Funds] = TableQuery[Funds]
    val prices: TableQuery[Prices] = TableQuery[Prices]
    val holdings: TableQuery[Holdings] = TableQuery[Holdings]
    val transactions: TableQuery[Transactions] = TableQuery[Transactions]


    db.withSession {
      implicit session =>
        transactions.delete
        holdings.delete
        prices.delete
        funds.delete
        users.delete
    }
  }

  // TODO - use generics to replace FileHoldingFactory, FileTransactionFactory & FilePriceFactory with a single class
  def reloadHoldings() = {
    val holdingPattern = """holdings.*_([a-zA-Z]+)\.txt""".r
    for (
      aFile <- new File(reportHome).listFiles(isAFileWithUserNameMatching(holdingPattern))
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
      aFile <- new File(reportHome).listFiles(isAFileWithUserNameMatching(transactionPattern))
    ) {
      val fileName = aFile.getName
      val transactionPattern(userName) = fileName
      val transactions = new FileTransactionFactory(s"$reportHome/$fileName").getTransactions()

      println(s"Persisting transactions for user $userName, file $fileName")
      new DatabasePersister().persistTransactions(userName, transactions)
    }
  }

  def reloadPrices() = {
    val pricePattern = """prices.*\.txt""".r
    for (
      aFile <- new File(reportHome).listFiles(isAFileWithoutUserNameMatching(pricePattern))
    ) {
      val fileName = aFile.getName
      val prices = new FilePriceFactory(s"$reportHome/$fileName").getPrices()

      println(s"Persisting prices, file $fileName")
      new DatabasePersister().persistPrices(prices)
    }
  }

  deleteAllRows()

  // Give transaction prices precedence over holdings for calculation purposes
  reloadTransactions()
  reloadHoldings()
  reloadPrices()
}