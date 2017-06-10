package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.database.PKs.PK
import models.org.ludwiggj.finance.persistence.database.Tables._
import play.api.Play.current
import play.api.db.DB
import scala.slick.driver.MySQLDriver.simple._

object User {
  lazy val db = Database.forDataSource(DB.getDataSource("finance"))

  def get(username: String): Option[UserRow] = {
    db.withSession {
      implicit session =>

        def getUserByName() =
          Users.filter {
            _.name === username
          }

        getUserByName().firstOption
    }
  }

  def insert(username: String, password: Option[String] = None) = {
    db.withSession {
      implicit session =>
        (Users returning Users.map(_.id)) += UserRow(PK[UserTable](0L), username, password)
    }
  }

  def getOrInsert(username: String): PK[UserTable] = {
    get(username) match {
      case Some(aUser: UserRow) => aUser.id
      case _ => insert(username)
    }
  }

  def authenticate(username: String, password: String): Int = {
    db.withSession { implicit session =>
      val q1 = for (u <- Users if u.name === username && u.password === password) yield u
      println("^^^^^^^^" + Query(q1.length).first)
      Query(q1.length).first
    }
  }
}