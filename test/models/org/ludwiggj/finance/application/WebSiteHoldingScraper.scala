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

  def getHoldings(user: User): List[Holding] = {
    WebSiteHoldingFactory(loginFormBuilder, user.name).getHoldings() map {
      holding => holding.copy(userName = user.reportName)
    }
  }

  def generatePeristedHoldingsFileName(userReportName: String) = {
    val date = DateTime.now.toString(DateTimeFormat.forPattern("yy_MM_dd"))
    s"$reportHome/holdings_${date}_${userReportName}.txt"
  }

  def persistHoldings(userReportName: String, holdings: List[Holding]): Unit = {
    val peristedHoldingsFileName = generatePeristedHoldingsFileName(userReportName)

    val persister = FilePersister(peristedHoldingsFileName)

    persister.write(holdings)

    HoldingsDatabase().insert(holdings)
  }

  def processHoldings(user: User) = Future {
    val userName = user.name

    time(s"processHoldings($userName)",
      try {
        val holdings = getHoldings(user)

        val holdingsTotal = holdings map (h => h.value) sum

        println(s"Total holdings ($userName): £$holdingsTotal")

        persistHoldings(user.reportName, holdings)

        (userName, holdingsTotal)
      } catch {
        case ex: NotAuthenticatedException =>
          val errorMsg = s"Cannot retrieve holdings for $userName [NotAuthenticatedException]"
          println(errorMsg)
          (userName, BigDecimal(0))
      }
    )
  }

  def composeWaitingFuture(fut: Future[(String, BigDecimal)], atMost: FiniteDuration, userName: String) =
    (Future {
      Await.result(fut, atMost)
    }
      recover {
      case e: ElementNotFoundException =>
        println(s"Problem retrieving details for $userName, returning £0 for this user.\n"
          + s"Error: ${e.toString}")
        (userName, BigDecimal(0))
    })

  val config = WebSiteConfig("cofunds")

  val loginFormBuilder = aLoginForm().basedOnConfig(config)

  val users = config.getUserList()

  private val application = FakeApplication()

  Play.start(application)

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
          val totalHoldings = results.foldLeft(BigDecimal(0))((runningTotal, result) => runningTotal + result._2)
          println(s"Results... $results Total £$totalHoldings")
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