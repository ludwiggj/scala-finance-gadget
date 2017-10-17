package models.org.ludwiggj.finance.persistence.database

import play.api.Logger
import slick.basic.DatabaseConfig
import slick.dbio.DBIO
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

class DatabaseLayer(val dbConfig: DatabaseConfig[JdbcProfile]) extends Tables with Profile {
  lazy val profile: JdbcProfile = dbConfig.profile
  val db = dbConfig.db

  def exec[T](action: DBIO[T]): T = Await.result(
    db.run(action).recover{
      case ex: Throwable =>
        Logger.error("Database query exception", ex)
        throw ex
    },
    2 seconds)
}