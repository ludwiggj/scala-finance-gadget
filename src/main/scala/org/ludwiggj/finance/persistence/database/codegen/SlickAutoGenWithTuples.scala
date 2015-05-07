package org.ludwiggj.finance.persistence.database.codegen

import scala.slick.driver.MySQLDriver
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.codegen.SourceCodeGenerator
import scala.slick.model.Model

object SlickAutoGenWithTuples {
  def main(args: Array[String]) {

    val db = Database.forConfig("db")

    val model = db.withSession { implicit session =>
      MySQLDriver.profile.createModel()
    }

    case class SourceGen(model: Model) extends SourceCodeGenerator(model) {
      override def Table = new Table(_) {
        override lazy val EntityType = new EntityType {
          override val classEnabled: Boolean = false
        }

        override lazy val PlainSqlMapper = new PlainSqlMapper {
          override val enabled = false
        }
      }
    }

    SourceGen(model).writeToFile(
      "scala.slick.driver.MySQLDriver", "src/main/scala", "org.ludwiggj.finance.persistence.database", "Tables", "Tables.scala"
    )
  }
}