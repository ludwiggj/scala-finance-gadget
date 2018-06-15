package utils

import java.util.concurrent.TimeoutException
import com.github.nscala_time.time.Imports.{DateTime, DateTimeFormat}
import com.typesafe.config.ConfigFactory
import models.org.ludwiggj.finance.domain.{FundName, Holding, Price}
import models.org.ludwiggj.finance.json.HoldingParser
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

object WebSiteHoldingScraper extends App {

  lazy val app = GuiceApplicationBuilder(configuration = Configuration.load(Environment.simple(), Map(
    "config.resource" -> "application.conf"
  ))).build()

  val databaseLayer = new DatabaseLayer(app.injector.instanceOf[DatabaseConfigProvider].get)

  import databaseLayer._

  private val config = ConfigFactory.load("acme")
  private val users = (config.getConfigList("site.userAccounts").asScala map (User(_))).toList

  private def getHoldings(user: User): Future[(User, List[Holding])] = Future {
    val userName = user.name

    time(s"processHoldings($userName)", {
      val webFacade = WebFacade(user, config)
      webFacade.login()

      val holdings: List[Holding] = HoldingParser.fromJsonString(
        webFacade.get("holdings"), user.reportName
      ).filter(
        h => (h.name != FundName("Cash")) && (h.name != FundName("Pending Trades"))
      )

      webFacade.logout()

      val holdingsTotal = holdings.map(h => h.value).sum
      println(s"Total holdings ($userName): £$holdingsTotal")

      (user, holdings)
    })
  }

  private def processHoldings(listOfUserHoldings: List[(User, List[Holding])]): Unit = {
    val date = DateTime.now.toString(DateTimeFormat.forPattern("yy_MM_dd"))

    def persistHoldingsToFile(userReportName: String, holdings: List[Holding]): Unit = {
      FilePersister(fileName = s"$holdingsHome/holdings_${date}_$userReportName.txt").write(holdings)
    }

    def persistPricesToFile(userReportName: String, prices: List[Price]): Unit = {
      FilePersister(fileName = s"$pricesHome/prices_${date}_$userReportName.txt").write(prices)
    }

    for {
      (user, holdings) <- listOfUserHoldings
    } {
      persistHoldingsToFile(user.reportName, holdings)

      val prices = holdings.map(_.price)
      persistPricesToFile(user.reportName, prices)
      exec(Prices.insert(prices))
    }
  }

  private def displayTotalHoldingAmount(listOfUserHoldings: List[(User, List[Holding])]): Unit = {
    val allHoldings = listOfUserHoldings.flatMap(_._2)
    val totalHoldingAmount = allHoldings.map(h => h.value).sum
    println(s"Total £$totalHoldingAmount")
  }

  private def scrape(): Unit = {
    time("Whole thing",
      try {
        val listOfFutures: List[Future[(User, List[Holding])]] = users map getHoldings

        val combinedFuture: Future[List[(User, List[Holding])]] = Future.sequence(listOfFutures)

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