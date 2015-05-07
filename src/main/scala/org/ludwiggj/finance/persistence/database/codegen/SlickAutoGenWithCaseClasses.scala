package org.ludwiggj.finance.persistence.database.codegen

object SlickAutoGenWithCaseClasses {
    def main(args: Array[String]) {
        scala.slick.codegen.SourceCodeGenerator.main(
            Array("scala.slick.driver.MySQLDriver", "com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/finance", "src/main/scala", "org.ludwiggj.finance.persistence.database", "finance", "gecko")
        )
    }
}