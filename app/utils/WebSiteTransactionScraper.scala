package utils

import com.gargoylesoftware.htmlunit.ElementNotFoundException
import com.github.nscala_time.time.Imports.{DateTime, DateTimeFormat}
import play.api.{Configuration, Environment, Play}
import play.api.inject.guice.GuiceApplicationBuilder
import java.util.concurrent.TimeoutException
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import models.org.ludwiggj.finance.builders.LoginFormBuilder._
import models.org.ludwiggj.finance.domain.Transaction
import models.org.ludwiggj.finance.persistence.file.FilePersister
import models.org.ludwiggj.finance.web.{NotAuthenticatedException, User, WebSiteConfig, WebSiteTransactionFactory}

object WebSiteTransactionScraper extends App {

  private val config = WebSiteConfig("cofunds")
  private val loginFormBuilder = aLoginForm().basedOnConfig(config)
  private val users = config.getUserList()
  private val date = DateTime.now.toString(DateTimeFormat.forPattern("yy_MM_dd"))

  def getTransactions(user: User): Future[(User, List[Transaction])] = Future {
    def getTransactions(): List[Transaction] = {
      WebSiteTransactionFactory(loginFormBuilder, user.name).getTransactions() map {
        tx => tx.copy(userName = user.reportName)
      }
    }

    val userName = user.name
    val emptyResult = (user, List())

    time(s"processTransactions($userName)",
      try {
        val transactions: List[Transaction] = getTransactions()

        println(s"Total transactions ($userName): ${transactions size}")

        (user, transactions)
      } catch {
        case ex: ElementNotFoundException =>
          println(s"Cannot retrieve transactions for $userName [ElementNotFoundException]. Error ${ex.toString}")
          emptyResult

        case ex: NotAuthenticatedException =>
          println(s"Cannot retrieve transactions for $userName [NotAuthenticatedException]. Error ${ex.toString}")
          emptyResult
      }
    )
  }

  def scrape() = {
    def processTransactions(listOfUserTransactions: List[(User, List[Transaction])]) = {
      def persistTransactionsToFile(user: User, transactions: List[Transaction]): Unit = {
        def generatePersistedTransactionsFileName(user: User): String = {
          s"$dataHome/txs_${date}_${user.reportName}.txt"
        }

        val persister = FilePersister(generatePersistedTransactionsFileName(user))
        persister.write(transactions)
      }

      for {
        (user, transactions) <- listOfUserTransactions
      } {
        persistTransactionsToFile(user, transactions)
        Transaction.insert(transactions)
      }
    }

    time("Whole thing",
      try {
        val listOfFutures = users map { user =>
          getTransactions(user)
        }

        val combinedFuture = Future.sequence(listOfFutures)

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

  // Start here
  val appConfig = Configuration.load(Environment.simple(), Map("config.resource" -> "application.conf"))
  implicit val application = new GuiceApplicationBuilder(configuration = appConfig).build()

  try {
    Play.start(application)
    scrape()
  } finally {
    Play.stop(application)
  }
}