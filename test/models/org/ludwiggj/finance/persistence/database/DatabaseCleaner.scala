package models.org.ludwiggj.finance.persistence.database

import scala.io.Source
import play.api.db.DB
import play.api.Play.current

object DatabaseCleaner {

  def getDdls(sqlFiles: List[String]) = for {
    sqlFile <- sqlFiles
    evolutionContent = Source.fromFile(s"conf/evolutions/finance/$sqlFile").getLines.mkString("\n")
    splitEvolutionContent = evolutionContent.split("# --- !Ups")
    upsDowns = splitEvolutionContent(1).split("# --- !Downs")
  } yield (upsDowns(1), upsDowns(0))

  def executeDbStatements(statements: List[String]) = {
    DB.withConnection("finance") { implicit connection =>

      for (ddl <- statements) {
        connection.createStatement.execute(ddl)
      }
    }
  }

  def recreateDb() = {
    val ddls = getDdls(List("1.sql", "2.sql", "3.sql", "4.sql", "5.sql", "6.sql", "7.sql"))

    val dropDdls = (ddls map {
      _._1
    }).reverse

    val createDdls = ddls map {
      _._2
    }

    executeDbStatements(dropDdls ++ createDdls)
  }
}