package models.org.ludwiggj.finance.persistence.database

import scala.io.Source
import play.api.db.DB
import play.api.Play.current

import scala.util.{Failure, Success, Try}

object Database {

  type Evolution = (String, String)

  private def getDdls(sqlFileNumber:Int = 1, ddls: List[Evolution] = List()): List[Evolution] = {
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
    DB.withConnection("finance") { implicit connection =>

      for (ddl <- statements) {
        connection.createStatement.execute(ddl)
      }
    }
  }

  def recreate() = {
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