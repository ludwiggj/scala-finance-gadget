package controllers

import java.sql.Date
import java.util
import models.org.ludwiggj.finance.persistence.database.TransactionsDatabase
import org.joda.time.DateTime
import play.api.mvc._
import models.org.ludwiggj.finance.domain.{FinanceDate, CashDelta}
import models.org.ludwiggj.finance.Portfolio
import models.org.ludwiggj.finance.domain.FinanceDate.sqlDateToFinanceDate
import models.org.ludwiggj.finance.dateTimeToDate

import scala.collection.immutable.SortedMap

class Portfolios extends Controller {

  def onDate(date: Date) = Action {
    implicit request =>
      val portfolios = Portfolio.get(date)

      val grandTotal = portfolios.foldRight(CashDelta(0, 0))(
        (portfolio, delta) => delta.add(portfolio.delta)
      )

      Ok(views.html.portfolios.details(portfolios, date, grandTotal))
  }

  def all(numberOfYearsAgoOption: Option[Int]) = Action {
    implicit request =>
      val transactionsDatabase = TransactionsDatabase()
      val investmentDates = transactionsDatabase.getRegularInvestmentDates().map(sqlDateToFinanceDate)

      def investmentDatesAFromPreviousYear(numberOfYearsAgoOfInterest: Int) = {
        val now = new DateTime()
        val beforeDate: util.Date = now.minusYears(numberOfYearsAgoOfInterest)
        val afterDate: util.Date = now.minusYears(numberOfYearsAgoOfInterest + 1)

        investmentDates.filter { d =>
          d.after(afterDate) && d.before(beforeDate)
        }
      }

      def earlierDataIsAvailable(numberOfYearsAgoOfInterest: Int) = {
        !investmentDatesAFromPreviousYear(numberOfYearsAgoOfInterest).isEmpty
      }

      def getPortfolios(investmentDatesOfInterest: List[FinanceDate]): Map[FinanceDate, (List[Portfolio], CashDelta)] = {
        def getInvestmentDates(): List[FinanceDate] = {
          val latestDates = transactionsDatabase.getTransactionsDatesSince(investmentDates.head).map(sqlDateToFinanceDate)
          latestDates ++ investmentDatesOfInterest
        }

        val portfolios = (getInvestmentDates() map (date => {
          val thePortfolios = Portfolio.get(date)
          val grandTotal = thePortfolios.foldRight(CashDelta(0, 0))(
            (portfolio, delta) => delta.add(portfolio.delta)
          )

          (date, (thePortfolios, grandTotal))
        }))

        implicit val Ord = implicitly[Ordering[FinanceDate]]
        SortedMap(portfolios: _*)(Ord.reverse)
      }

      val numberOfYearsAgo = numberOfYearsAgoOption.getOrElse(0)
      val investmentDatesOfInterest = investmentDatesAFromPreviousYear(numberOfYearsAgo)

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