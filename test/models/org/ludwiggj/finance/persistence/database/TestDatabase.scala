package models.org.ludwiggj.finance.persistence.database

import play.api.db.DB

import scala.io.Source

//TODO - refactor out!
import play.api.Play.current

import scala.util.{Failure, Success, Try}

object TestDatabase {

  type Evolution = (String, String)

  private def getDdls(sqlFileNumber: Int = 1, ddls: List[Evolution] = List()): List[Evolution] = {
    // TODO - finance, get from config?
    val evolutionContent = Try(Source.fromFile(s"conf/evolutions/finance/${sqlFileNumber}.sql").getLines.mkString("\n"))
    evolutionContent match {
      case Success(evolutionStr) => {
        val upsDowns = evolutionStr.split("# --- !Ups")(1).split("# --- !Downs")
        getDdls(sqlFileNumber + 1, (upsDowns(1), upsDowns(0)) :: ddls)
      }

      case Failure(ex) => ddls.reverse
    }
  }

  private def executeDbStatements(statements: List[String]) = {
    def dbName = current.configuration.underlying.getString("db_name")

    DB.withConnection(dbName) { implicit connection =>

      for (ddl <- statements) {
        connection.createStatement.execute(ddl)
      }
    }
  }

  def recreateSchema() = {
    val ddls = getDdls()

    val dropDdls = (ddls map {
      _._1
    }).reverse

    val createDdls = ddls map {
      _._2
    }

    executeDbStatements(dropDdls ++ createDdls)
  }
}