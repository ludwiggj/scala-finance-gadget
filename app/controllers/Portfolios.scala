package controllers

import models.org.ludwiggj.finance.dateTimeToDate
import models.org.ludwiggj.finance.domain.{FinanceDate, PortfolioList, Portfolio, Transaction}
import models.org.ludwiggj.finance.domain.FinanceDate.sqlDateToFinanceDate
import java.sql.Date
import java.util
import org.joda.time.DateTime
import play.api.mvc._
import play.api.cache._
import play.api.Play.current
import javax.inject.Inject

import scala.collection.immutable.SortedMap

class Portfolios @Inject()(cache: CacheApi) extends Controller {

  private def getPortfolioDataOnDate(date: Date): PortfolioList = {
    cache.getOrElse[PortfolioList](s"portfolios-$date") {
      new PortfolioList(Portfolio.get(date))
    }
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

  private def yearsAgoCacheKey(prefix: String) = { (header: RequestHeader) =>
    prefix + header.getQueryString("yearsAgo").getOrElse("0")
  }

  def all(numberOfYearsAgoOption: Option[Int]) = Cached(yearsAgoCacheKey("portfolioDataAll-yearsAgo-")) {
    // To switch caching off...
    // def all(numberOfYearsAgoOption: Option[Int]) = {
    Action {
      implicit request =>
        val investmentDates = cache.getOrElse[List[FinanceDate]]("investmentDates") {
          Transaction.getRegularInvestmentDates().map(sqlDateToFinanceDate)
        }

        def investmentDatesFromAPreviousYear(numberOfYearsAgoOfInterest: Int) = {
          val now = new DateTime()
          val beforeDate: util.Date = now.minusYears(numberOfYearsAgoOfInterest)
          val afterDate: util.Date = now.minusYears(numberOfYearsAgoOfInterest + 1)

          investmentDates.filter { d =>
            d.after(afterDate) && d.before(beforeDate)
          }
        }

        def earlierDataIsAvailable(numberOfYearsAgoOfInterest: Int) = {
          !investmentDatesFromAPreviousYear(numberOfYearsAgoOfInterest).isEmpty
        }

        def getPortfolios(investmentDatesOfInterest: List[FinanceDate]): Map[FinanceDate, PortfolioList] = {
          def getInvestmentDates(): List[FinanceDate] = {
            val latestDates = Transaction.getTransactionsDatesSince(investmentDates.head).map(sqlDateToFinanceDate)
            latestDates ++ investmentDatesOfInterest
          }

          val portfolios = (getInvestmentDates() map (date => {
            val thePortfolios = getPortfolioDataOnDate(date)

            (date, thePortfolios)
          }))

          implicit val Ord = implicitly[Ordering[FinanceDate]]
          SortedMap(portfolios: _*)(Ord.reverse)
        }

        val numberOfYearsAgo = numberOfYearsAgoOption.getOrElse(0)
        val investmentDatesOfInterest = investmentDatesFromAPreviousYear(numberOfYearsAgo)

        if (investmentDatesOfInterest.isEmpty) {
          BadRequest(s"This was a bad request: yearsAgo=$numberOfYearsAgo")
        } else {
          val oneYearEarlier = numberOfYearsAgo + 1
          val previousYear = if (earlierDataIsAvailable(oneYearEarlier)) Some(oneYearEarlier) else None

          val oneYearLater = numberOfYearsAgo - 1
          val nextYear = if (oneYearLater >= 0) Some(oneYearLater) else None

          val portfolios = getPortfolios(investmentDatesOfInterest)

          Ok(views.html.portfolios.all(portfolios, previousYear, nextYear))
        }
    }
  }
}