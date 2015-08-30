package models.org.ludwiggj.finance.persistence.database

import play.api.db.DB
import play.api.Play.current
import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._
import Tables.{Users,UsersRow}

/**
 * Data access facade.
 */

class UsersDatabase {
  lazy val db = Database.forDataSource(DB.getDataSource("finance"))


  def getOrInsert(user: UsersRow): Long = {
    val users: TableQuery[Users] = TableQuery[Users]
    db.withSession {
      implicit session =>

        def insertUser() = {
          (users returning users.map(_.id)) += user
        }

        val getUserByName = users.filter {
          _.name === user.name
        }

        if (getUserByName.exists.run) {
          getUserByName.first.id
        } else {
          insertUser()
        }
    }
  }
}

// Note: declaring a Users companion object would break the <> mapping.
object UsersDatabase {
  def apply() = new UsersDatabase()

  implicit def usersRowWrapper(name: String) = UsersRow(0L, name)
}