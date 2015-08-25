package org.ludwiggj.finance.persistence.database

import org.ludwiggj.finance.persistence.database.Tables.{UsersRow, Users}
import scala.slick.driver.MySQLDriver.simple._

/**
 * Data access facade.
 * Note: declaring a Fund companion object breaks the <> mapping.
 */
object UserDatabase {

  /**
   * Adds the given product to the database.
   */
//  def insert(product: Product): Int = {
//    Database.forDataSource(DB.getDataSource()) withSession {
//      Products.insert(product)
//    }
//  }

  val db = Database.forConfig("db")

  def getOrInsertUser(user: UsersRow): Long = {
    val users: TableQuery[Users] = TableQuery[Users]
    db.withSession {
      implicit session =>

        def insertUser() = {
          (users returning users.map(_.id)) += user
        }

        val getUserByName = users.filter {
          _.name === user.name
        }

        if (!getUserByName.exists.run) {
          insertUser()
        } else {
          getUserByName.first.id
        }
    }
  }
}