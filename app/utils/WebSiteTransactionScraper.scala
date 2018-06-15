package utils

import java.util.concurrent.TimeoutException

import com.github.nscala_time.time.Imports.{DateTime, DateTimeFormat}
import com.typesafe.config.ConfigFactory
import models.org.ludwiggj.finance.domain.Transaction
import models.org.ludwiggj.finance.json.TransactionParser
import models.org.ludwiggj.finance.persistence.database.DatabaseLayer
import models.org.ludwiggj.finance.persistence.file.FilePersister
import models.org.ludwiggj.finance.web._
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Configuration, Environment, Play}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import scala.collection.JavaConverters._

object WebSiteTransactionScraper extends App {

  lazy val app = GuiceApplicationBuilder(configuration = Configuration.load(Environment.simple(), Map(
    "config.resource" -> "application.conf"
  ))).build()

  val databaseLayer = new DatabaseLayer(app.injector.instanceOf[DatabaseConfigProvider].get)

  import databaseLayer._

  private val config = ConfigFactory.load("acme")
  private val users = (config.getConfigList("site.userAccounts").asScala map (User(_))).toList
  private val date = DateTime.now.toString(DateTimeFormat.forPattern("yy_MM_dd"))

  private def getTransactions(user: User): Future[(User, List[Transaction])] = Future {
    val userName = user.name

    time(s"processTransactions($userName)", {
      val webFacade = WebFacade(user, config)
      webFacade.login()

      val transactions: List[Transaction] = TransactionParser.fromJsonString(
        webFacade.get("transactions"), user.reportName
      )

      webFacade.logout()

      println(s"Total transactions ($userName): ${transactions size}")

      (user, transactions)
    })
  }

  private def processTransactions(listOfUserTransactions: List[(User, List[Transaction])]): Unit = {
    def persistTransactionsToFile(user: User, transactions: List[Transaction]): Unit = {
      def generatePersistedTransactionsFileName(user: User): String = {
        s"$transactionsHome/txs_${date}_${user.reportName}.txt"
      }

      val persister = FilePersister(generatePersistedTransactionsFileName(user))
      persister.write(transactions)
    }


    // Start here
    for {
      (user, transactions) <- listOfUserTransactions
    } {
      persistTransactionsToFile(user, transactions)
      exec(Transactions.insert(transactions.filter {
        _.shouldBePersistedIntoDb
      }))
    }
  }

  private def scrape(): Unit = {
    time("Whole thing",
      try {
        val listOfFutures: List[Future[(User, List[Transaction])]] = users map { user =>
          getTransactions(user)
        }

        val combinedFuture: Future[List[(User, List[Transaction])]] = Future.sequence(listOfFutures)

        Await.ready(combinedFuture, 30 seconds).value.get match {
          case Success(listOfUserTransactions) =>
            processTransactions(listOfUserTransactions)
            println(s"Done...")

          case Failure(ex) =>
            println(s"Oh dear... ${ex.getMessage}")
            ex.printStackTrace()
        }
      } catch {
        case ex: TimeoutException => println(ex.getMessage)
      }
    )
  }

  try {
    Play.start(app)
    scrape()
  } finally {
    Play.stop(app)

    // Kludge to force the app to terminate
    System.exit(0)
  }
}