package models.org.ludwiggj.finance.persistence.database

import play.api.db.DB
import play.api.Play.current
import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._
import Tables.{Users, UsersRow}

class UsersDatabase private {
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

// Note: declaring a Users companion object would break the <> mapping.
object UsersDatabase {
  def apply() = new UsersDatabase()

  implicit def stringToUsersRow(name: String) = UsersRow(0L, name)
}