package controllers

import javax.inject.Inject

import models.org.ludwiggj.finance.persistence.database.DatabaseLayer
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc._
import slick.driver.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class Application @Inject()(dbConfigProvider: DatabaseConfigProvider) extends Controller {

  def index = Action {
    Redirect(routes.Portfolios.all())
  }

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  val databaseLayer = new DatabaseLayer(dbConfig.driver)
  import databaseLayer._
  import profile.api._

  def exec[T](action: DBIO[T]): T = Await.result(db.run(action), 2 seconds)

  lazy val loginForm = Form(
    Forms.tuple(
      "username" -> text,
      "password" -> text) verifying("Invalid username or password", result => result match {
      case (username, password) => {
        println("username=" + username + " password=" + password);
        val userList = exec(Users.authenticate(username, password))
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