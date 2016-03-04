package models.org.ludwiggj.finance.application

import java.util.concurrent.TimeoutException

import com.gargoylesoftware.htmlunit.ElementNotFoundException
import com.github.nscala_time.time.Imports.{DateTime, DateTimeFormat}
import models.org.ludwiggj.finance.builders.LoginFormBuilder._
import models.org.ludwiggj.finance.domain.Holding
import models.org.ludwiggj.finance.persistence.database.HoldingsDatabase
import models.org.ludwiggj.finance.persistence.file.FilePersister
import models.org.ludwiggj.finance.web.{User, NotAuthenticatedException, WebSiteConfig, WebSiteHoldingFactory}
import play.api.Play
import play.api.test.FakeApplication

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object WebSiteHoldingScraper extends App {

  private val config = WebSiteConfig("cofunds")
  private val loginFormBuilder = aLoginForm().basedOnConfig(config)
  private val users = config.getUserList()
  private val application = FakeApplication()

  def getHoldings(user: User): List[Holding] = {
    WebSiteHoldingFactory(loginFormBuilder, user.name).getHoldings() map {
      holding => holding.copy(userName = user.reportName)
    }
  }

  def generatePersistedHoldingsFileName(userReportName: String) = {
    val date = DateTime.now.toString(DateTimeFormat.forPattern("yy_MM_dd"))
    s"$reportHome/holdings_${date}_${userReportName}.txt"
  }

  def persistHoldingsToFile(userReportName: String, holdings: List[Holding]): Unit = {
    val peristedHoldingsFileName = generatePersistedHoldingsFileName(userReportName)

    FilePersister(peristedHoldingsFileName).write(holdings)
  }

  def processHoldings(user: User) = Future[List[Holding]] {
    val userName = user.name

    time(s"processHoldings($userName)",
      try {
        val holdings = getHoldings(user)

        val holdingsTotal = holdings.map(h => h.value).sum
        println(s"Total holdings ($userName): £$holdingsTotal")

        persistHoldingsToFile(user.reportName, holdings)

        holdings
      } catch {
        case ex: NotAuthenticatedException =>
          val errorMsg = s"Cannot retrieve holdings for $userName [NotAuthenticatedException]"
          println(errorMsg)
          List()
      }
    )
  }

  def composeWaitingFuture(fut: Future[List[Holding]], atMost: FiniteDuration, userName: String) =
    (Future {
      Await.result(fut, atMost)
    }
      recover {
      case e: ElementNotFoundException =>
        println(s"Problem retrieving details for $userName, returning £0 for this user.\n"
          + s"Error: ${e.toString}")
        List()
    })

  def scrape() = {
    time("Whole thing",
      try {
        val listOfFutures = users map { user =>
          composeWaitingFuture(
            processHoldings(user), 30 seconds, user.name
          )
        }

        val combinedFuture = Future.sequence(listOfFutures)

        Await.ready(combinedFuture, 31 seconds).value.get match {
          case Success(results) =>
            val holdings = results.flatten

            val totalHoldings = holdings.map(h => h.value).sum
            println(s"Total £$totalHoldings")

            HoldingsDatabase().insert(holdings)

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

  // Start here
  Play.start(application)
  scrape()
}