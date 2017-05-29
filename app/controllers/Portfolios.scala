package controllers

import models.org.ludwiggj.finance.dateTimeToDate
import models.org.ludwiggj.finance.domain.FinanceDate.sqlDateToFinanceDate
import models.org.ludwiggj.finance.domain.{FinanceDate, PortfolioList, Transaction}
import models.org.ludwiggj.finance.web.User
import java.sql.Date
import java.util
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.mvc._
import play.api.cache._
import scala.concurrent.duration._
import scala.collection.immutable.SortedMap

class Portfolios @Inject()(cache: CacheApi) extends Controller with Secured {

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login())

  private def getPortfolioDataOnDate(username: String, date: Date): PortfolioList = {
    println(s"getPortfolioDataOnDate: $username-portfolio-$date")
    cache.getOrElse[PortfolioList](s"$username-portfolio-$date", 5.minutes) {
      if (User.isAdmin(username)) PortfolioList.get(date) else PortfolioList.get(date, username)
    }
  }

  def onDate(date: Date) = {
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
        val allInvestmentDates = cache.getOrElse[List[FinanceDate]](s"$username-allInvestmentDates", 5.minutes) {
          Transaction.getRegularInvestmentDates().map(sqlDateToFinanceDate)
        }

        def investmentDates(yearOffset: Int) = {
          val now = new DateTime()
          val beforeDate: util.Date = now.minusYears(yearOffset)
          val afterDate: util.Date = now.minusYears(yearOffset + 1)

          allInvestmentDates.filter { d =>
            d.after(afterDate) && d.before(beforeDate)
          }
        }

        def getPortfolios(investmentDates: List[FinanceDate]): Map[FinanceDate, PortfolioList] = {
          def investmentDatesSince(date: FinanceDate): List[FinanceDate] = {
            if (User.isAdmin(username))
              Transaction.getDatesSince(date).map(sqlDateToFinanceDate)
            else
              Transaction.getDatesSince(date, username).map(sqlDateToFinanceDate)
          }

          val allDates = investmentDatesSince(allInvestmentDates.head) ++ investmentDates

          val portfolios = (allDates map (date => {
            val thePortfolios = getPortfolioDataOnDate(username, date)

            (date, thePortfolios)
          }))

          implicit val Ord = implicitly[Ordering[FinanceDate]]
          SortedMap(portfolios: _*)(Ord.reverse)
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