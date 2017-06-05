package utils

import java.io.{File, FilenameFilter}

import models.org.ludwiggj.finance.domain.{Price, Transaction, User}
import models.org.ludwiggj.finance.persistence.database.Tables._
import models.org.ludwiggj.finance.persistence.file.{FilePriceFactory, FileTransactionFactory}
import play.api.{Configuration, Environment, Play}
import play.api.db.DB
import play.api.inject.guice.GuiceApplicationBuilder

import scala.slick.driver.MySQLDriver.simple._
import scala.util.matching.Regex

object DatabaseReload extends App {

  def isAFileWithUserNameMatching(pattern: Regex) = new FilenameFilter {
    override def accept(dir: File, name: String) = {
      name match {
        case pattern(_) => true
        case _ => false
      }
    }
  }

  def isAFileMatching(pattern: Regex) = new FilenameFilter {
    override def accept(dir: File, name: String) = {
      pattern.findFirstIn(name).isDefined
    }
  }

  def deleteAllRows() = {
    lazy val db = Database.forDataSource(DB.getDataSource("finance"))
    val users: TableQuery[Users] = TableQuery[Users]
    val funds: TableQuery[Funds] = TableQuery[Funds]
    val prices: TableQuery[Prices] = TableQuery[Prices]
    val transactions: TableQuery[Transactions] = TableQuery[Transactions]


    db.withSession {
      implicit session =>
        transactions.delete
        prices.delete
        funds.delete
        users.delete
    }
  }

  def reloadUserAccounts() = {
    User.insert(UsersRow(0, "Admin", Some("Admin")))
    User.insert(UsersRow(0, "Me", Some("Me")))
    User.insert(UsersRow(0, "Spouse", Some("Spouse")))
  }

  def reloadTransactions() = {
    val transactionPattern =
      """txs.*_([a-zA-Z]+)\.txt""".r
    for (
      aFile <- new File(dataHome).listFiles(isAFileWithUserNameMatching(transactionPattern))
    ) {
      val fileName = aFile.getName
      val transactionPattern(userName) = fileName
      val transactions = FileTransactionFactory(userName, s"$dataHome/$fileName").getTransactions()

      println(s"Persisting transactions for user $userName, file $fileName")
      Transaction.insert(transactions)
    }
  }

  def reloadPrices() = {
    val pricePattern = """prices.*\.txt""".r
    for (
      aFile <- new File(dataHome).listFiles(isAFileMatching(pricePattern))
    ) {
      val fileName = aFile.getName
      println(s"Persisting prices, file $fileName")
      Price.insert(FilePriceFactory(s"$dataHome/$fileName").getPrices())
    }
  }

  val config = Configuration.load(Environment.simple(), Map("config.resource" -> "application.conf"))
  implicit val application = new GuiceApplicationBuilder(configuration = config).build()

  try {
    Play.start(application)
    deleteAllRows()
    reloadUserAccounts()
    reloadTransactions()
    reloadPrices()
  } finally {
    Play.stop(application)
  }
}