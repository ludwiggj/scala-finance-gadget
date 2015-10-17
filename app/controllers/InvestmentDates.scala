package controllers

import models.org.ludwiggj.finance.persistence.database.TransactionsDatabase
import models.org.ludwiggj.finance.domain.FinanceDate.sqlDateToFinanceDate
import play.api.mvc._

class InvestmentDates extends Controller {

  def list = Action {
    implicit request =>
      val investmentDates = TransactionsDatabase().getRegularInvestmentDates().map(sqlDateToFinanceDate)
      Ok(views.html.investmentDates.list(investmentDates))
  }
}