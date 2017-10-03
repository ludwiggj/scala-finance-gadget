package controllers

import javax.inject.Inject

import models.org.ludwiggj.finance.LocalDateOrdering
import models.org.ludwiggj.finance.domain.PortfolioList
import models.org.ludwiggj.finance.persistence.database.DatabaseLayer
import models.org.ludwiggj.finance.web.User
import org.joda.time.LocalDate
import play.api.cache._
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc._
import slick.driver.JdbcProfile

import scala.collection.immutable.SortedMap
import scala.concurrent.duration._
import scala.language.postfixOps

class Portfolios @Inject()(cache: CacheApi, dbConfigProvider: DatabaseConfigProvider) extends Controller with Secured {

  val databaseLayer = new DatabaseLayer(dbConfigProvider.get[JdbcProfile])
  import databaseLayer._

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login())

  private def getPortfolioDataOnDate(username: String, date: LocalDate): PortfolioList = {
    println(s"getPortfolioDataOnDate: $username-portfolio-$date")
    cache.getOrElse[PortfolioList](s"$username-portfolio-$date", 5.minutes) {
      if (User.isAdmin(username)) PortfolioList.get(date) else PortfolioList.get(date, username)
    }
  }

  def onDate(date: LocalDate) = {
    IsAuthenticated { username =>
      implicit request =>
        val portfolios = getPortfolioDataOnDate(username, date)
        Ok(views.html.portfolios.details(portfolios, date))
    }
  }

  def all(page: Int) = {
    IsAuthenticated { username =>
      implicit request =>
        def transactionDatesSince(date: LocalDate): List[LocalDate] = {
          if (User.isAdmin(username))
            exec(Transactions.getDatesSince(date))
          else
            exec(Transactions.getDatesSince(date, username))
        }

        val allPortfolioDates = cache.getOrElse[List[LocalDate]](s"$username-allPortfolioDates", 5.minutes) {
          exec(Transactions.getRegularInvestmentDates()) match {
            case dateList if (dateList.isEmpty) => dateList // TODO - should fetch all tx'es here?
            case dateList => transactionDatesSince(dateList.head) ++ dateList
          }
        }

        def portfolioDatesForPage(page: Int) = {
          val noOfItemsPerPage = 12

          allPortfolioDates.drop((page - 1) * noOfItemsPerPage).take(noOfItemsPerPage)
        }

        // TODO - This (and other methods in this class) are not tested
        def getPortfolios(dates: List[LocalDate]): Map[LocalDate, PortfolioList] = {
          val portfolios = (dates map (date => {
            val thePortfolios = getPortfolioDataOnDate(username, date)

            (date, thePortfolios)
          }))

          SortedMap(portfolios: _*)(LocalDateOrdering.reverse)
        }

        def adjacentPages() = {
          def earlierDataIsAvailable = {
            !portfolioDatesForPage(page + 1).isEmpty
          }

          val previousPage = if (earlierDataIsAvailable) Some(page + 1) else None

          val nextPage = if (page >= 2) Some(page - 1) else None

          (previousPage, nextPage)
        }

        // Start here
        val portfolioDates = portfolioDatesForPage(page)

        if (portfolioDates.isEmpty) {
          if (page == 1) {
            BadRequest(s"You have no investments!")
          } else {
            BadRequest(s"No data for page [$page]")
          }
        } else {
          val portfolios = getPortfolios(portfolioDates)
          val (previousPage, nextPage) = adjacentPages()
          Ok(views.html.portfolios.all(portfolios, previousPage, nextPage))
        }
    }
  }
}