package utils

import java.io.{File, FilenameFilter}

import models.org.ludwiggj.finance.domain.{Price, Transaction}
import models.org.ludwiggj.finance.persistence.database.Tables._
import models.org.ludwiggj.finance.persistence.file.{FilePriceFactory, FileTransactionFactory}
import play.api.{Configuration, Environment, Play}
import play.api.db.DB
import play.api.inject.guice.GuiceApplicationBuilder
import scala.slick.driver.H2Driver.simple._
import scala.util.matching.Regex

object DatabaseReload extends App {

  private def isAFileWithUserNameMatching(pattern: Regex) = new FilenameFilter {
    override def accept(dir: File, name: String) = {
      name match {
        case pattern(_) => true
        case _ => false
      }
    }
  }

  private def isAFileMatching(pattern: Regex) = new FilenameFilter {
    override def accept(dir: File, name: String) = {
      pattern.findFirstIn(name).isDefined
    }
  }

  private def deleteAllData() = {
    def dbName = Play.current.configuration.underlying.getString("db_name")
    lazy val db = Database.forDataSource(DB.getDataSource(dbName))
    val users: TableQuery[UserTable] = TableQuery[UserTable]
    val funds: TableQuery[FundTable] = TableQuery[FundTable]
    val prices: TableQuery[PriceTable] = TableQuery[PriceTable]
    val transactions: TableQuery[TransactionTable] = TableQuery[TransactionTable]

    db.withSession {
      implicit session =>
        transactions.delete
        prices.delete
        funds.delete
        users.filterNot(_.name inSet List("Admin", "Me", "Spouse")).delete
    }
  }

  private def reloadTransactions() = {
    val transactionPattern =
      """txs.*_([a-zA-Z]+)\.txt""".r
    for (
      aFile <- new File(transactionsHome).listFiles(isAFileWithUserNameMatching(transactionPattern))
    ) {
      val fileName = aFile.getName
      val transactionPattern(userName) = fileName
      val transactions = FileTransactionFactory(userName, s"$transactionsHome/$fileName").getTransactions()

      println(s"Persisting transactions for user $userName, file $fileName")
      Transaction.insert(transactions)
    }
  }

  private def reloadPrices() = {
    val pricePattern = """prices.*\.txt""".r
    for (
      aFile <- new File(pricesHome).listFiles(isAFileMatching(pricePattern))
    ) {
      val fileName = aFile.getName
      println(s"Persisting prices, file $fileName")
      Price.insert(FilePriceFactory(s"$pricesHome/$fileName").getPrices())
    }
  }

  val config = Configuration.load(Environment.simple(), Map("config.resource" -> "application.conf"))
  implicit val application = new GuiceApplicationBuilder(configuration = config).build()

  try {
    Play.start(application)
    deleteAllData()
    reloadTransactions()
    reloadPrices()
  } finally {
    Play.stop(application)
  }
}