package utils

import java.io.{File, FilenameFilter}
import models.org.ludwiggj.finance.domain.{Price, Transaction}
import models.org.ludwiggj.finance.persistence.database.DatabaseLayer
import models.org.ludwiggj.finance.persistence.file.{FilePriceFactory, FileTransactionFactory}
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Environment, Play}
import play.api.inject.guice.GuiceApplicationBuilder
import scala.util.matching.Regex

object DatabaseReload extends App {

  lazy val app = GuiceApplicationBuilder(configuration = Configuration.load(Environment.simple(), Map(
    "config.resource" -> "application.conf"
  ))).build()

  val databaseLayer = new DatabaseLayer(app.injector.instanceOf[DatabaseConfigProvider].get)

  import databaseLayer._
  import profile.api._

  private def isAFileWithUserNameMatching(pattern: Regex): FilenameFilter = (_: File, name: String) => {
    name match {
      case pattern(_) => true
      case _ => false
    }
  }

  private def isAFileMatching(pattern: Regex): FilenameFilter = (_: File, name: String) => {
    pattern.findFirstIn(name).isDefined
  }

  private def deleteAllData() = {
    exec(Transactions.delete)
    exec(Prices.delete)
    exec(Funds.delete)
    exec(Users.filterNot(_.name inSet List("Admin", "Me", "Spouse")).delete)
  }

  private def reloadTransactions(): Unit = {
    val transactionPattern =
      """txs.*_([a-zA-Z]+)\.txt""".r

    val txsToPersist: List[Transaction] = (for {
      aFile <- new File(transactionsHome).listFiles(isAFileWithUserNameMatching(transactionPattern))
      fileName = aFile.getName
      transactionPattern(userName) = fileName
      _ = println(s"Reading transactions for [$userName], file [$fileName]")
    } yield FileTransactionFactory(userName, s"$transactionsHome/$fileName").fetchTransactions().filter(
      _.shouldBePersistedIntoDb
    )).toList.flatten

    // Persisting transactions in smaller groups to avoid DB errors
    // Tried to configure DB connection to avoid this, but to no avail
    val persistInGroups: List[Transaction] => Unit = (txsToPersist: List[Transaction]) => {
      val txGroupSize = 25
      println(s"About to persist ${txsToPersist.length} transactions in groups of $txGroupSize")

      txsToPersist.grouped(txGroupSize).toList.zipWithIndex.foreach { case (transactions, index) =>
        val startTxNumber = (index * txGroupSize) + 1
        println(s"Persisting tx's $startTxNumber - ${startTxNumber + transactions.length - 1}")
        exec(Transactions.insert(transactions))
      }
    }

    persistInGroups(txsToPersist)
  }

  private def reloadPrices(): Unit = {
    val pricePattern = """prices.*\.txt""".r

    val pricesToPersist: List[Price] = (for {
      aFile <- new File(pricesHome).listFiles(isAFileMatching(pricePattern))
      fileName = aFile.getName
      _ = println(s"Reading prices, file [$fileName]")
    } yield FilePriceFactory(s"$pricesHome/$fileName").fetchPrices()
      ).toList.flatten

    // Persisting prices in smaller groups to avoid DB errors
    // Tried to configure DB connection to avoid this, but to no avail
    val persistInGroups: List[Price] => Unit = (pricesToPersist: List[Price]) => {
      val priceGroupSize = 50
      println(s"About to persist ${pricesToPersist.length} prices in groups of $priceGroupSize")

      pricesToPersist.grouped(priceGroupSize).toList.zipWithIndex.foreach { case (prices, index) =>
        val startPriceNumber = (index * priceGroupSize) + 1
        println(s"Persisting prices $startPriceNumber - ${startPriceNumber + prices.length - 1}")
        exec(Prices.insert(prices))
      }
    }

    persistInGroups(pricesToPersist)
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