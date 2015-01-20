package org.ludwiggj.finance.database.codegen

object SlickAutoGen {
    def main(args: Array[String]) {
        scala.slick.codegen.SourceCodeGenerator.main(
            Array("scala.slick.driver.MySQLDriver", "com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/finance", "src/main/scala", "org.ludwiggj.finance.database", "finance", "gecko")
        )
    }
}