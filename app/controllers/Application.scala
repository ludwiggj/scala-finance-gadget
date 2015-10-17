package controllers

import play.api.mvc._

/**
 * Minimal controller examples that output text/plain responses.
 */
class Application extends Controller {

  def index = Action {
    Redirect(routes.InvestmentDates.list())
  }
}