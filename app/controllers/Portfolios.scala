package controllers

import java.sql.Date

import models.org.ludwiggj.finance.persistence.database.TransactionsDatabase
import org.joda.time.DateTime
import play.api.mvc._
import models.org.ludwiggj.finance.domain.{FinanceDate, CashDelta}
import models.org.ludwiggj.finance.Portfolio
import models.org.ludwiggj.finance.domain.FinanceDate.sqlDateToFinanceDate

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

  def all = Action {
    implicit request =>
      Ok(views.html.portfolios.all(getPortfolios()))
  }

  private def getPortfolios(): Map[FinanceDate, (List[Portfolio], CashDelta)] = {
    def getInvestmentDates(): List[FinanceDate] = {
      val transactionsDatabase = TransactionsDatabase()

      val investmentDates = transactionsDatabase.getRegularInvestmentDates().map(sqlDateToFinanceDate)
      val latestDates = transactionsDatabase.getTransactionsDatesSince(investmentDates.head).map(sqlDateToFinanceDate)

      // should be implicit!
      val latestInvestmentDate = investmentDates.head.date

      val latestInvestmentDateMinusAYear = new DateTime(latestInvestmentDate).minusYears(1).toDate();

      latestDates ++ investmentDates.filter {
        _.after(latestInvestmentDateMinusAYear)
      }
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
}