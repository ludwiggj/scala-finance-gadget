package models.org.ludwiggj.finance.persistence.database

import scala.io.Source
import play.api.db.DB
import play.api.Play.current

object DatabaseCleaner {

  val sqlFiles = List("1.sql", "2.sql", "3.sql", "4.sql", "5.sql", "6.sql")

  val ddls = for {
    sqlFile <- sqlFiles
    evolutionContent = Source.fromFile(s"conf/evolutions/finance/$sqlFile").getLines.mkString("\n")
    splitEvolutionContent = evolutionContent.split("# --- !Ups")
    upsDowns = splitEvolutionContent(1).split("# --- !Downs")
  } yield (upsDowns(1), upsDowns(0))

  val dropDdls = (ddls map {
    _._1
  }).reverse

  val createDdls = ddls map {
    _._2
  }

  def recreateDb() = {
    DB.withConnection("finance") { implicit connection =>

      for (ddl <- dropDdls ++ createDdls) {
        connection.createStatement.execute(ddl)
      }
    }
  }
}