package models.org.ludwiggj.finance.persistence.database

import slick.backend.DatabaseConfig
import slick.dbio.DBIO
import slick.driver.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

class DatabaseLayer(val dbConfig: DatabaseConfig[JdbcProfile]) extends Tables with Profile {
  lazy val profile: JdbcProfile = dbConfig.driver
  val db = dbConfig.db

  def exec[T](action: DBIO[T]): T = Await.result(
    db.run(action).recover{
      // TODO - log the exception?
      case ex: Throwable => throw ex
    },
    2 seconds)
}