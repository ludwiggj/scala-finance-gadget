package controllers

import models.org.ludwiggj.finance.dateTimeToDate
import models.org.ludwiggj.finance.domain.{FinanceDate, PortfolioList, Portfolio, Transaction}
import models.org.ludwiggj.finance.domain.FinanceDate.sqlDateToFinanceDate
import java.sql.Date
import java.util
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.mvc._
import play.api.cache._
import play.api.Play.current

import scala.collection.immutable.SortedMap

class Portfolios @Inject()(cache: CacheApi) extends Controller {

  private def getPortfolioDataOnDate(date: Date): PortfolioList = {
    cache.getOrElse[PortfolioList](s"portfolios-$date") {
      new PortfolioList(Portfolio.get(date))
    }
  }

  private def yearsAgoCacheKey(prefix: String) = { (header: RequestHeader) =>
    prefix + header.getQueryString("yearsAgo").getOrElse("0")
  }

  def onDate(date: Date) = Cached(s"portfolioDataOnDate-$date") {
    // To switch caching off...
    // def onDate(date: Date) = {
    Action {
      implicit request =>
        val portfolios = getPortfolioDataOnDate(date)

        Ok(views.html.portfolios.details(portfolios, date))
    }
  }

  def all(yearOffset: Int) = Cached(yearsAgoCacheKey("portfolioData-yearOffset-")) {
    // To switch caching off...
    // def all(numberOfYearsAgo: Int) = {
    Action {
      implicit request =>
        val allInvestmentDates = cache.getOrElse[List[FinanceDate]]("allInvestmentDates") {
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
            Transaction.getTransactionsDatesSince(date).map(sqlDateToFinanceDate)
          }

          val allDates = investmentDatesSince(allInvestmentDates.head) ++ investmentDates

          val portfolios = (allDates map (date => {
            val thePortfolios = getPortfolioDataOnDate(date)

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