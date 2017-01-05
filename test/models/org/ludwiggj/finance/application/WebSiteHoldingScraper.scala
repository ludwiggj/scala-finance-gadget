package models.org.ludwiggj.finance.application

import java.util.concurrent.TimeoutException

import com.gargoylesoftware.htmlunit.ElementNotFoundException
import com.github.nscala_time.time.Imports.{DateTime, DateTimeFormat}
import models.org.ludwiggj.finance.builders.LoginFormBuilder._
import models.org.ludwiggj.finance.domain.{Holding, Price}
import models.org.ludwiggj.finance.persistence.file.FilePersister
import models.org.ludwiggj.finance.web.{NotAuthenticatedException, User, WebSiteConfig, WebSiteHoldingFactory}
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

  def getHoldings(user: User) = Future[(User, List[Holding])] {
    def getHoldings(): List[Holding] = {
      WebSiteHoldingFactory(loginFormBuilder, user.name).getHoldings() map {
        holding => holding.copy(userName = user.reportName)
      }
    }

    val userName = user.name
    val emptyResult = (user, List())

    time(s"processHoldings($userName)",
      try {
        val holdings = getHoldings()

        val holdingsTotal = holdings.map(h => h.value).sum
        println(s"Total holdings ($userName): £$holdingsTotal")

        (user, holdings)
      } catch {
        case ex: NotAuthenticatedException =>
          println(s"Cannot retrieve holdings for $userName [NotAuthenticatedException]. Error ${ex.toString}")
          emptyResult

        case ex: ElementNotFoundException =>
          println(s"Cannot retrieve holdings for $userName [ElementNotFoundException]. Error ${ex.toString}")
          emptyResult
      }
    )
  }

  def scrape() = {
    def processHoldings(listOfUserHoldings: List[(User, List[Holding])]) = {
      val date = DateTime.now.toString(DateTimeFormat.forPattern("yy_MM_dd"))

      def persistHoldingsToFile(userReportName: String, holdings: List[Holding]): Unit = {
        FilePersister(fileName = s"$holdingsHome/holdings_${date}_${userReportName}.txt").write(holdings)
      }

      def persistPricesToFile(userReportName: String, prices: List[Price]): Unit = {
        FilePersister(fileName = s"$dataHome/prices_${date}_${userReportName}.txt").write(prices)
      }

      for {
        (user, holdings) <- listOfUserHoldings
      } {
        persistHoldingsToFile(user.reportName, holdings)

        val prices = holdings.map(_.price)
        persistPricesToFile(user.reportName, prices)
        Price.insert(prices)
      }
    }

    def displayTotalHoldingAmount(listOfUserHoldings: List[(User, List[Holding])]) = {
      val allHoldings = listOfUserHoldings.map(_._2).flatten
      val totalHoldingAmount = allHoldings.map(h => h.value).sum
      println(s"Total £$totalHoldingAmount")
    }

    // scrape(): start here
    time("Whole thing",
      try {
        val listOfFutures = users map { user =>
          getHoldings(user)
        }

        val combinedFuture = Future.sequence(listOfFutures)

        Await.ready(combinedFuture, 30 seconds).value.get match {
          case Success(listOfUserHoldings) =>
            processHoldings(listOfUserHoldings)
            displayTotalHoldingAmount(listOfUserHoldings)
            println(s"Done...")

          case Failure(ex) =>
            println(s"Oh dear... ${ex.getMessage}")
            ex.printStackTrace()
        }
      } catch {
        case ex: TimeoutException => println(ex.getMessage)
      }
      finally {
        Play.stop(application)
      }
    )
  }

  // Start here
  Play.start(application)
  scrape()
}