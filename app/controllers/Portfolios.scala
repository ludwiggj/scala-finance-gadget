package controllers

import javax.inject.Inject

import models.org.ludwiggj.finance.LocalDateOrdering
import models.org.ludwiggj.finance.domain.{PortfolioList, Transaction}
import models.org.ludwiggj.finance.web.User
import org.joda.time.LocalDate
import play.api.cache._
import play.api.mvc._
import scala.collection.immutable.SortedMap
import scala.concurrent.duration._

class Portfolios @Inject()(cache: CacheApi) extends Controller with Secured {

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

  def all(yearOffset: Int) = {
    IsAuthenticated { username =>
      implicit request =>
        println(s"all: $username-allInvestmentDates")
        val allInvestmentDates = cache.getOrElse[List[LocalDate]](s"$username-allInvestmentDates", 5.minutes) {
          Transaction.getRegularInvestmentDates()
        }

        def investmentDates(yearOffset: Int) = {
          val now = new LocalDate()
          val beforeDate = now.minusYears(yearOffset)
          val afterDate = now.minusYears(yearOffset + 1)

          import LocalDateOrdering._

          allInvestmentDates.filter { d =>
            d < beforeDate && d > afterDate
          }
        }

        def getPortfolios(investmentDates: List[LocalDate]): Map[LocalDate, PortfolioList] = {
          def investmentDatesSince(date: LocalDate): List[LocalDate] = {
            if (User.isAdmin(username))
              Transaction.getDatesSince(date)
            else
              Transaction.getDatesSince(date, username)
          }

          val allDates = investmentDatesSince(allInvestmentDates.head) ++ investmentDates

          val portfolios = (allDates map (date => {
            val thePortfolios = getPortfolioDataOnDate(username, date)

            (date, thePortfolios)
          }))

          SortedMap(portfolios: _*)(LocalDateOrdering.reverse)
        }

        def adjacentYearOffsets() = {
          def earlierDataIsAvailable(yearOffset: Int) = {
            !investmentDates(yearOffset).isEmpty
          }

          val oneYearEarlier = yearOffset + 1
          val previousYear = if (earlierDataIsAvailable(oneYearEarlier)) Some(oneYearEarlier) else None

          val oneYearLater = yearOffset - 1
          val nextYear = if (oneYearLater >= 0) Some(oneYearLater) else None

          (previousYear, nextYear)
        }

        // Start here
        val investmentDatesOfInterest = investmentDates(yearOffset)

        if (investmentDatesOfInterest.isEmpty) {
          BadRequest(s"This was a bad request: yearsAgo=$yearOffset")
        } else {
          val portfolios = getPortfolios(investmentDatesOfInterest)
          val (previousYear, nextYear) = adjacentYearOffsets()
          Ok(views.html.portfolios.all(portfolios, previousYear, nextYear))
        }
    }
  }
}