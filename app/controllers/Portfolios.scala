package controllers

import java.sql.Date

import play.api.mvc._
import models.org.ludwiggj.finance.domain.CashDelta
import models.org.ludwiggj.finance.Portfolio
import models.org.ludwiggj.finance.domain.FinanceDate.sqlDateToFinanceDate

class Portfolios extends Controller {

  def show(date: Date) = Action {
    implicit request =>
      val portfolios = Portfolio.get(date)

      val grandTotal = portfolios.foldRight(CashDelta(0, 0))(
        (portfolio: Portfolio, delta: CashDelta) => delta.add(portfolio.delta)
      )

      Ok(views.html.portfolios.details(portfolios, date, grandTotal))
  }
}