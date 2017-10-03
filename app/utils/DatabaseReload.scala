package utils

import java.io.{File, FilenameFilter}

import models.org.ludwiggj.finance.persistence.database.DatabaseLayer
import models.org.ludwiggj.finance.persistence.file.{FilePriceFactory, FileTransactionFactory}
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Environment, Play}
import play.api.inject.guice.GuiceApplicationBuilder
import scala.util.matching.Regex

object DatabaseReload extends App {

  lazy val app = new GuiceApplicationBuilder(configuration = Configuration.load(Environment.simple(), Map(
    "config.resource" -> "application.conf"
  ))).build()

  val databaseLayer = new DatabaseLayer(app.injector.instanceOf[DatabaseConfigProvider].get)

  import databaseLayer._
  import profile.api._

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
    exec(Transactions.delete)
    exec(Prices.delete)
    exec(Funds.delete)
    exec(Users.filterNot(_.name inSet List("Admin", "Me", "Spouse")).delete)
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

      println(s"Persisting transactions for [$userName], file [$fileName]")
      exec(Transactions.insert(transactions))
    }
  }

  private def reloadPrices() = {
    val pricePattern = """prices.*\.txt""".r
    for (
      aFile <- new File(pricesHome).listFiles(isAFileMatching(pricePattern))
    ) {
      val fileName = aFile.getName
      println(s"Persisting prices, file [$fileName]")
      exec(Prices.insert(FilePriceFactory(s"$pricesHome/$fileName").getPrices()))
    }
  }

  try {
    Play.start(app)
    deleteAllData()
    reloadTransactions()
    reloadPrices()
  } finally {
    Play.stop(app)

    // Kludge to force the app to terminate
    System.exit(0)
  }
}