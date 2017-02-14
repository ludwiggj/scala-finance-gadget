package models.org.ludwiggj.finance.application

import java.io.{File, FilenameFilter}
import models.org.ludwiggj.finance.domain.{Price, Transaction, User}
import models.org.ludwiggj.finance.persistence.database.Tables.{Funds, Prices, Transactions, Users}
import models.org.ludwiggj.finance.persistence.file.{FilePriceFactory, FileTransactionFactory}
import models.org.ludwiggj.finance.persistence.database.Tables.UsersRow
import play.api.Play
import play.api.Play.current
import play.api.db.DB
import play.api.test.FakeApplication
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

  private val application = FakeApplication()

  Play.start(application)

  deleteAllRows()
  reloadUserAccounts()
  reloadTransactions()
  reloadPrices()

  Play.stop(application)
}