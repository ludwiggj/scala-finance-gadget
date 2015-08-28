package org.ludwiggj.finance.application

import java.util.concurrent.TimeoutException

import com.gargoylesoftware.htmlunit.ElementNotFoundException
import com.github.nscala_time.time.Imports.{DateTime, DateTimeFormat}
import models.org.ludwiggj.finance.builders.LoginFormBuilder
import models.org.ludwiggj.finance.builders.LoginFormBuilder._
import models.org.ludwiggj.finance.persistence.database.DatabasePersister
import models.org.ludwiggj.finance.persistence.file.FilePersister
import models.org.ludwiggj.finance.web.{NotAuthenticatedException, WebSiteConfig, WebSiteTransactionFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object FinanceTransactionScraper extends App {

  def processTransactions(accountName: String) = Future {
    time(s"processTransactions($accountName)",
      try {
        val transactionFactory = WebSiteTransactionFactory(loginFormBuilder, accountName)
        val transactions = transactionFactory.getTransactions()

        println(s"Total transactions ($accountName): ${transactions size}")

        val persister = FilePersister(s"$reportHome/txs_${date}_${accountName}.txt")

        persister.write(transactions)
        new DatabasePersister().persistTransactions(accountName, transactions)
      } catch {
        case ex: ElementNotFoundException =>
          println(s"Cannot retrieve transactions for $accountName, ${ex.toString}")
        case ex: NotAuthenticatedException =>
          println(s"Cannot retrieve transactions for $accountName [NotAuthenticatedException]")
      })
  }

  def composeWaitingFuture(fut: Future[Unit], atMost: FiniteDuration, accountName: String) =
    (Future {
      Await.result(fut, atMost)
    }
      recover {
      case e: ElementNotFoundException =>
        println(s"Problem retrieving details for $accountName, Error: ${e.toString}")
    })

  val config = WebSiteConfig("cofunds.conf")

  val loginFormBuilder = aLoginForm().basedOnConfig(config)

  val accounts = config.getAccountList()

  val date = DateTime.now.toString(DateTimeFormat.forPattern("yy_MM_dd"))

  val listOfFutures = accounts map { account =>
    composeWaitingFuture(
      processTransactions(account.name), 30 seconds, account.name
    )
  }

  val combinedFuture = Future.sequence(listOfFutures)

  time("Whole thing",
    try {
      Await.ready(combinedFuture, 31 seconds).value.get match {
        case Success(results) =>
          println(s"Done...")
        case Failure(ex) =>
          println(s"Oh dear... ${ex.getMessage}")
      }
    } catch {
      case ex: TimeoutException => println(ex.getMessage)
    }
  )
}