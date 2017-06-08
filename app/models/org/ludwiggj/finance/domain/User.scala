package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.database.PKs.PK
import models.org.ludwiggj.finance.persistence.database.Tables._
import play.api.Play.current
import play.api.db.DB

import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._

object User {
  implicit def stringToUsersRow(name: String) = UserRow(PK[UserTable](0L), name)

  lazy val db = Database.forDataSource(DB.getDataSource("finance"))

  def get(userName: String): Option[UserRow] = {
    db.withSession {
      implicit session =>

        def getUserByName() =
          Users.filter {
            _.name === userName
          }

        getUserByName().firstOption
    }
  }

  def insert(user: UserRow) = {
    db.withSession {
      implicit session =>
        (Users returning Users.map(_.id)) += user
    }
  }

  def getOrInsert(user: UserRow): PK[UserTable] = {
    db.withSession {
      implicit session =>
        get(user.name) match {
          case Some(aUser: UserRow) => aUser.id
          case _ => insert(user)
        }
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