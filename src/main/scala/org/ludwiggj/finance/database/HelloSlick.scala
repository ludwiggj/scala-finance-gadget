package org.ludwiggj.finance.database

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta.MTable

object HelloSlick extends App {

  val suppliers: TableQuery[Suppliers] = TableQuery[Suppliers]
  val coffees: TableQuery[Coffees] = TableQuery[Coffees]

  // Create a connection (called a "session") to an in-memory H2 database
  val db = Database.forConfig("db")

  def drop(tables: TableQuery[_ <: Table[_]]*)(implicit session: Session) {
    tables foreach {
      table =>
        if (!MTable.getTables(table.baseTableRow.tableName).list.isEmpty) table.ddl.drop
    }
  }

  def create(tables: TableQuery[_ <: Table[_]]*)(implicit session: Session) {
    tables foreach {
      _.ddl.create
    }
  }

  db.withSession { implicit session =>
    // Drop and recreate the schema
    drop(coffees, suppliers)
    create(suppliers, coffees)

    // Insert some suppliers
    suppliers += (101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199")
    suppliers += (49, "Superior Coffee", "1 Party Place", "Mendocino", "TX", "95460")
    suppliers += (150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966")

    def showAllSuppliers {
      println(s"All suppliers:\n${suppliers.list}")
    }

    // Query the Coffees table using a foreach and print each row
    def showIndividualSuppliers {
      suppliers foreach { case (supId, supName, street, city, state, zip) =>
        println(s"Supplier [supId: $supId, supName: $supName, street: $street, city: $city, state: $state, zip: $zip]")
      }
    }

    showAllSuppliers
    showIndividualSuppliers
  }
}