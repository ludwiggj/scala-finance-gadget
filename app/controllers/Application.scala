package controllers

import javax.inject.Inject

import models.org.ludwiggj.finance.persistence.database.DatabaseLayer
import play.api.Logger
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc._
import slick.jdbc.JdbcProfile

class Application @Inject()(dbConfigProvider: DatabaseConfigProvider) extends InjectedController {

  def index = Action {
    Redirect(routes.Portfolios.all())
  }

  val databaseLayer = new DatabaseLayer(dbConfigProvider.get[JdbcProfile])
  import databaseLayer._

  lazy val loginForm = Form(
    Forms.tuple(
      "username" -> text,
      "password" -> text) verifying("Invalid username or password", result => result match {
      case (username, password) => {
        val authenticated = exec(Users.authenticate(username, password)) == 1
        Logger.info(s"authenticate user [$username] result [$authenticated]")
        authenticated
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
  self: InjectedController =>

  // Retrieve the username.
  def username(request: RequestHeader) = request.session.get("username")

  // Redirect to login if the user is not authorized.
  def onUnauthorized(request: RequestHeader): Result

  def IsAuthenticated(f: => String => Request[AnyContent] => Result) =
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
}