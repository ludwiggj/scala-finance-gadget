package org.ludwiggj.finance.application

import java.util.concurrent.TimeoutException

import com.gargoylesoftware.htmlunit.ElementNotFoundException
import com.github.nscala_time.time.Imports.{DateTime, DateTimeFormat}
import models.org.ludwiggj.finance.builders.LoginFormBuilder._
import models.org.ludwiggj.finance.domain.Holding
import models.org.ludwiggj.finance.persistence.database.DatabasePersister
import models.org.ludwiggj.finance.persistence.file.FilePersister
import models.org.ludwiggj.finance.web.{NotAuthenticatedException, WebSiteConfig, WebSiteHoldingFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object FinanceHoldingScraper extends App {

  def getHoldings(accountName: String): List[Holding] = {
    WebSiteHoldingFactory(loginFormBuilder, accountName).getHoldings()
  }

  def generatePeristedHoldingsFileName(accountName: String) = {
    val date = DateTime.now.toString(DateTimeFormat.forPattern("yy_MM_dd"))
    s"$reportHome/holdings_${date}_${accountName}.txt"
  }

  def persistHoldings(accountName: String, holdings: List[Holding]): Unit = {
    val peristedHoldingsFileName = generatePeristedHoldingsFileName(accountName)

    val persister = FilePersister(peristedHoldingsFileName)

    persister.write(holdings)

    new DatabasePersister().persistHoldings(accountName, holdings)
  }

  def processHoldings(accountName: String) = Future {
    time(s"processHoldings($accountName)",
      try {
        val holdings = getHoldings(accountName)

        val holdingsTotal = holdings map (h => h.value) sum

        println(s"Total holdings ($accountName): £$holdingsTotal")

        persistHoldings(accountName, holdings)

        (accountName, holdingsTotal)
      } catch {
        case ex: NotAuthenticatedException =>
          val errorMsg = s"Cannot retrieve holdings for $accountName [NotAuthenticatedException]"
          println(errorMsg)
          (accountName, BigDecimal(0))
      }
    )
  }

  def composeWaitingFuture(fut: Future[(String, BigDecimal)], atMost: FiniteDuration, accountName: String) =
    (Future {
      Await.result(fut, atMost)
    }
      recover {
      case e: ElementNotFoundException =>
        println(s"Problem retrieving details for $accountName, returning £0 for this account.\n"
          + s"Error: ${e.toString}")
        (accountName, BigDecimal(0))
    })

  val config = WebSiteConfig("cofunds.conf")

  val loginFormBuilder = aLoginForm().basedOnConfig(config)

  val accounts = config.getAccountList()

  val listOfFutures = accounts map { account =>
    composeWaitingFuture(
      processHoldings(account.name), 30 seconds, account.name
    )
  }

  val combinedFuture = Future.sequence(listOfFutures)

  time("Whole thing",
    try {
      Await.ready(combinedFuture, 31 seconds).value.get match {
        case Success(results) =>
          val totalHoldings = results.foldLeft(BigDecimal(0))((runningTotal, result) => runningTotal + result._2)
          println(s"Results... $results Total £$totalHoldings")
        case Failure(ex) =>
          println(s"Oh dear... ${ex.getMessage}")
      }
    } catch {
      case ex: TimeoutException => println(ex.getMessage)
    }
  )
}