package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.database.Tables._
import play.api.Play.current
import play.api.db.DB

import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._

object User {
  implicit def stringToUsersRow(name: String) = UsersRow(0L, name)

  lazy val db = Database.forDataSource(DB.getDataSource("finance"))

  def get(userName: String): Option[UsersRow] = {
    db.withSession {
      implicit session =>

        def getUserByName() =
          Users.filter {
            _.name === userName
          }

        getUserByName().firstOption
    }
  }

  def insert(user: UsersRow) = {
    db.withSession {
      implicit session =>
        (Users returning Users.map(_.id)) += user
    }
  }

  def getOrInsert(user: UsersRow): Long = {
    db.withSession {
      implicit session =>
        get(user.name) match {
          case Some(aUser: UsersRow) => aUser.id
          case _ => insert(user)
        }
    }
  }
}