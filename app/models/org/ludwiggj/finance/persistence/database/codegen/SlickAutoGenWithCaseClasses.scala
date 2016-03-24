package org.ludwiggj.finance.persistence.database.codegen

import java.util.Properties
import scala.slick.driver.MySQLDriver

object SlickAutoGenWithCaseClasses {

  def main(args: Array[String]) = {
    codegen.writeToFile(
      "scala.slick.driver.MySQLDriver",
      "app",
      "models.org.ludwiggj.finance.persistence.database",
      "Tables",
      "Tables.scala"
    )
  }

  val db = MySQLDriver.simple.Database.forURL(
    "jdbc:mysql://localhost:3306/finance", "finance", "gecko", new Properties(), "com.mysql.jdbc.Driver")

  // filter out desired tables
  val included = Seq("FUNDS", "HOLDINGS", "PRICES", "TRANSACTIONS", "USERS")

  val model = db.withSession { implicit session =>
    val tables = MySQLDriver.defaultTables.toList.filter(t => included contains t.name.name)
    MySQLDriver.createModel(Some(tables))
  }

  val codegen = new scala.slick.codegen.SourceCodeGenerator(model) {
    // add custom code
    override def code = "\n%s\n%s\n%s\n".format(
      "// Graeme, added to handle BigDecimal bug",
      "import models.org.ludwiggj.finance.domain._",
      super.code)

    // override generator responsible for tables
    override def Table = new Table(_) {
      table =>

      // override generator responsible for columns
      override def Column = new Column(_) {
        // customize Scala column names
        override def rawName = (table.model.name.table, this.model.name) match {
          case ("HOLDINGS", "HOLDING_DATE") => "date"
          case ("PRICES", "PRICE_DATE") => "date"
          case ("TRANSACTIONS", "TRANSACTION_DATE") => "date"
          case _ => super.rawName
        }
      }
    }
  }
}