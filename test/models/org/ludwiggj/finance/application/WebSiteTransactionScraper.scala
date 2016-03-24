package models.org.ludwiggj.finance.application

import java.util.concurrent.TimeoutException

import com.gargoylesoftware.htmlunit.ElementNotFoundException
import com.github.nscala_time.time.Imports.{DateTime, DateTimeFormat}
import models.org.ludwiggj.finance.Transaction
import models.org.ludwiggj.finance.builders.LoginFormBuilder._
import models.org.ludwiggj.finance.persistence.file.FilePersister
import models.org.ludwiggj.finance.web.{NotAuthenticatedException, WebSiteConfig, WebSiteTransactionFactory, User}
import play.api.Play
import play.api.test.FakeApplication

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object WebSiteTransactionScraper extends App {

  private val config = WebSiteConfig("cofunds")
  private val loginFormBuilder = aLoginForm().basedOnConfig(config)
  private val users = config.getUserList()
  private val date = DateTime.now.toString(DateTimeFormat.forPattern("yy_MM_dd"))
  private val application = FakeApplication()

  def getTransactions(user: User): List[Transaction] = {
    val transactionFactory = WebSiteTransactionFactory(loginFormBuilder, user.name)

    val transactions = transactionFactory.getTransactions() map {
      tx => tx.copy(userName = user.reportName)
    }
    transactions
  }

  def generatePersistedTransactionsFileName(user: User): String = {
    s"$reportHome/txs_${date}_${user.reportName}.txt"
  }

  def persistTransactionsToFile(user: User, transactions: List[Transaction]): Unit = {
    val persister = FilePersister(generatePersistedTransactionsFileName(user))
    persister.write(transactions)
  }

  def processTransactions(user: User): Future[List[Transaction]] = Future {
    val userName = user.name

    time(s"processTransactions($userName)",
      try {
        val transactions: List[Transaction] = getTransactions(user)

        println(s"Total transactions ($userName): ${transactions size}")

        persistTransactionsToFile(user, transactions)
        transactions
      } catch {
        case ex: ElementNotFoundException =>
          println(s"Cannot retrieve transactions for $userName, ${ex.toString}")
          List()
        case ex: NotAuthenticatedException =>
          println(s"Cannot retrieve transactions for $userName [NotAuthenticatedException]")
          List()
      }
    )
  }

  def composeWaitingFuture(fut: Future[List[Transaction]], atMost: FiniteDuration, userName: String) =
    (Future {
      Await.result(fut, atMost)
    }
      recover {
      case e: ElementNotFoundException =>
        println(s"Problem retrieving details for $userName, Error: ${e.toString}")
        List()
    })

  def scrape() = {
    time("Whole thing",
      try {
        val listOfFutures = users map { user =>
          composeWaitingFuture(
            processTransactions(user), 30 seconds, user.name
          )
        }

        val combinedFuture = Future.sequence(listOfFutures)

        Await.ready(combinedFuture, 31 seconds).value.get match {
          case Success(results) =>
            Transaction.insert(results.flatten)
            println(s"Done...")

          case Failure(ex) =>
            println(s"Oh dear... ${ex.getMessage}")
            ex.printStackTrace()
        }
      } catch {
        case ex: TimeoutException => println(ex.getMessage)
      } finally {
        Play.stop(application)
      }
    )
  }

  // Start here
  Play.start(application)
  scrape()
}