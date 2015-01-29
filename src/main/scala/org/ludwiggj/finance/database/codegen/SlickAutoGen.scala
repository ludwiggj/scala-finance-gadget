package org.ludwiggj.finance.database.codegen

import scala.slick.driver.MySQLDriver
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.codegen.SourceCodeGenerator
import scala.slick.model.Model

object SlickAutoGen {
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
      "scala.slick.driver.MySQLDriver", "src/main/scala", "org.ludwiggj.finance.database", "Tables", "Tables.scala"
    )
  }
}