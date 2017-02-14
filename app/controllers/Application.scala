package controllers

import models.org.ludwiggj.finance.domain.User
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Redirect(routes.Portfolios.all())
  }

  lazy val loginForm = Form(
    Forms.tuple(
      "username" -> text,
      "password" -> text) verifying("Invalid username or password", result => result match {
      case (username, password) => {
        println("username=" + username + " password=" + password);
        val userList = User.authenticate(username, password)
        userList == 1
      }
      case _ => false
    }))

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  // Handle login form submission.
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      user => Redirect(routes.Portfolios.all()).withSession("username" -> user._1))
  }

  // Logout and clean the session.
  def logout = Action {
    Redirect(routes.Application.login).withNewSession.flashing(
      "success" -> "You've been logged out")
  }
}

trait Secured {
  self: Controller =>

  // Retrieve the username.
  def username(request: RequestHeader) = request.session.get("username")

  // Redirect to login if the user is not authorized.
  def onUnauthorized(request: RequestHeader): Result

  def IsAuthenticated(f: => String => Request[AnyContent] => Result) =
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
}