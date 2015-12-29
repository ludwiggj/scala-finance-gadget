package controllers

import models.org.ludwiggj.finance.domain.FinanceDate
import models.org.ludwiggj.finance.persistence.database.TransactionsDatabase
import models.org.ludwiggj.finance.domain.FinanceDate.sqlDateToFinanceDate
import play.api.mvc._

class InvestmentDates extends Controller {

  def list = Action {
    implicit request =>
      Ok(views.html.investmentDates.list(getInvestmentDates()))
  }

  def getInvestmentDates(): List[FinanceDate] = {
    val transactionsDatabase = TransactionsDatabase()

    val investmentDates = transactionsDatabase.getRegularInvestmentDates().map(sqlDateToFinanceDate)
    val latestDates = transactionsDatabase.getTransactionsDatesSince(investmentDates.head).map(sqlDateToFinanceDate)

    latestDates ++ investmentDates
  }
}