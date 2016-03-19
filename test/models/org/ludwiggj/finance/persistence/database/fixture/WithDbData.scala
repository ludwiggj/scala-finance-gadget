package models.org.ludwiggj.finance.persistence.database.fixture

import org.specs2.execute.{AsResult, Result}
import play.api.db.DB
import play.api.test.WithApplication

import scala.io.Source

trait WithDbData extends WithApplication {
  override def around[T: AsResult](t: => T): Result = super.around {
    setupData()
    t
  }

  private def sqlFiles = List("1.sql", "2.sql", "3.sql", "4.sql", "5.sql")

  private def ddls = for {
    sqlFile <- sqlFiles
    evolutionContent = Source.fromFile(s"conf/evolutions/finance/$sqlFile").getLines.mkString("\n")
    splitEvolutionContent = evolutionContent.split("# --- !Ups")
    upsDowns = splitEvolutionContent(1).split("# --- !Downs")
  } yield (upsDowns(1), upsDowns(0))

  private def dropDdls = (ddls map {
    _._1
  }).reverse

  private def createDdls = ddls map {
    _._2
  }

  private def dropCreateDb() = {
    DB.withConnection("finance") { implicit connection =>

      for (ddl <- dropDdls ++ createDdls) {
        connection.createStatement.execute(ddl)
      }
    }
  }

  def setupData() = {
    dropCreateDb()
  }
}