package models.org.ludwiggj.finance.application

import java.util.concurrent.TimeoutException

import com.gargoylesoftware.htmlunit.ElementNotFoundException
import com.github.nscala_time.time.Imports.{DateTime, DateTimeFormat}
import models.org.ludwiggj.finance.builders.LoginFormBuilder._
import models.org.ludwiggj.finance.persistence.database.TransactionsDatabase
import models.org.ludwiggj.finance.persistence.file.FilePersister
import models.org.ludwiggj.finance.web.{NotAuthenticatedException, WebSiteConfig, WebSiteTransactionFactory}
import play.api.Play
import play.api.test.FakeApplication

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object FinanceTransactionScraper extends App {

  def processTransactions(userName: String) = Future {
    time(s"processTransactions($userName)",
      try {
        val transactionFactory = WebSiteTransactionFactory(loginFormBuilder, userName)
        val transactions = transactionFactory.getTransactions()

        println(s"Total transactions ($userName): ${transactions size}")

        val persister = FilePersister(s"$reportHome/txs_${date}_${userName}.txt")

        persister.write(transactions)
        TransactionsDatabase().insert(transactions)
      } catch {
        case ex: ElementNotFoundException =>
          println(s"Cannot retrieve transactions for $userName, ${ex.toString}")
        case ex: NotAuthenticatedException =>
          println(s"Cannot retrieve transactions for $userName [NotAuthenticatedException]")
      })
  }

  def composeWaitingFuture(fut: Future[Unit], atMost: FiniteDuration, userName: String) =
    (Future {
      Await.result(fut, atMost)
    }
      recover {
      case e: ElementNotFoundException =>
        println(s"Problem retrieving details for $userName, Error: ${e.toString}")
    })

  val config = WebSiteConfig("cofunds.conf")

  val loginFormBuilder = aLoginForm().basedOnConfig(config)

  val users = config.getUserList()

  val date = DateTime.now.toString(DateTimeFormat.forPattern("yy_MM_dd"))

  private val application = FakeApplication()

  Play.start(application)

  time("Whole thing",
    try {
      val listOfFutures = users map { user =>
        composeWaitingFuture(
          processTransactions(user.name), 30 seconds, user.name
        )
      }

      val combinedFuture = Future.sequence(listOfFutures)

      Await.ready(combinedFuture, 31 seconds).value.get match {
        case Success(results) =>
          println(s"Done...")
        case Failure(ex) =>
          println(s"Oh dear... ${ex.getMessage}")
      }
    } catch {
      case ex: TimeoutException => println(ex.getMessage)
    } finally {
      Play.stop(application)
    }
  )
}