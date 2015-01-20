package org.ludwiggj.finance.database
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = scala.slick.driver.MySQLDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: scala.slick.driver.JdbcProfile
  import profile.simple._
  import scala.slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import scala.slick.jdbc.{GetResult => GR}
  
  /** DDL for all tables. Call .create to execute. */
  lazy val ddl = Coffees.ddl ++ Suppliers.ddl
  
  /** Entity class storing rows of table Coffees
   *  @param cofName Database column COF_NAME DBType(VARCHAR), PrimaryKey, Length(254,true)
   *  @param supId Database column SUP_ID DBType(INT)
   *  @param price Database column PRICE DBType(DOUBLE)
   *  @param sales Database column SALES DBType(INT)
   *  @param total Database column TOTAL DBType(INT) */
  case class CoffeesRow(cofName: String, supId: Int, price: Double, sales: Int, total: Int)
  /** GetResult implicit for fetching CoffeesRow objects using plain SQL queries */
  implicit def GetResultCoffeesRow(implicit e0: GR[String], e1: GR[Int], e2: GR[Double]): GR[CoffeesRow] = GR{
    prs => import prs._
    CoffeesRow.tupled((<<[String], <<[Int], <<[Double], <<[Int], <<[Int]))
  }
  /** Table description of table COFFEES. Objects of this class serve as prototypes for rows in queries. */
  class Coffees(_tableTag: Tag) extends Table[CoffeesRow](_tableTag, "COFFEES") {
    def * = (cofName, supId, price, sales, total) <> (CoffeesRow.tupled, CoffeesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (cofName.?, supId.?, price.?, sales.?, total.?).shaped.<>({r=>import r._; _1.map(_=> CoffeesRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column COF_NAME DBType(VARCHAR), PrimaryKey, Length(254,true) */
    val cofName: Column[String] = column[String]("COF_NAME", O.PrimaryKey, O.Length(254,varying=true))
    /** Database column SUP_ID DBType(INT) */
    val supId: Column[Int] = column[Int]("SUP_ID")
    /** Database column PRICE DBType(DOUBLE) */
    val price: Column[Double] = column[Double]("PRICE")
    /** Database column SALES DBType(INT) */
    val sales: Column[Int] = column[Int]("SALES")
    /** Database column TOTAL DBType(INT) */
    val total: Column[Int] = column[Int]("TOTAL")
    
    /** Foreign key referencing Suppliers (database name SUP_FK) */
    lazy val suppliersFk = foreignKey("SUP_FK", supId, Suppliers)(r => r.supId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Coffees */
  lazy val Coffees = new TableQuery(tag => new Coffees(tag))
  
  /** Entity class storing rows of table Suppliers
   *  @param supId Database column SUP_ID DBType(INT), PrimaryKey
   *  @param supName Database column SUP_NAME DBType(VARCHAR), Length(254,true)
   *  @param street Database column STREET DBType(VARCHAR), Length(254,true)
   *  @param city Database column CITY DBType(VARCHAR), Length(254,true)
   *  @param state Database column STATE DBType(VARCHAR), Length(254,true)
   *  @param zip Database column ZIP DBType(VARCHAR), Length(254,true) */
  case class SuppliersRow(supId: Int, supName: String, street: String, city: String, state: String, zip: String)
  /** GetResult implicit for fetching SuppliersRow objects using plain SQL queries */
  implicit def GetResultSuppliersRow(implicit e0: GR[Int], e1: GR[String]): GR[SuppliersRow] = GR{
    prs => import prs._
    SuppliersRow.tupled((<<[Int], <<[String], <<[String], <<[String], <<[String], <<[String]))
  }
  /** Table description of table SUPPLIERS. Objects of this class serve as prototypes for rows in queries. */
  class Suppliers(_tableTag: Tag) extends Table[SuppliersRow](_tableTag, "SUPPLIERS") {
    def * = (supId, supName, street, city, state, zip) <> (SuppliersRow.tupled, SuppliersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (supId.?, supName.?, street.?, city.?, state.?, zip.?).shaped.<>({r=>import r._; _1.map(_=> SuppliersRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column SUP_ID DBType(INT), PrimaryKey */
    val supId: Column[Int] = column[Int]("SUP_ID", O.PrimaryKey)
    /** Database column SUP_NAME DBType(VARCHAR), Length(254,true) */
    val supName: Column[String] = column[String]("SUP_NAME", O.Length(254,varying=true))
    /** Database column STREET DBType(VARCHAR), Length(254,true) */
    val street: Column[String] = column[String]("STREET", O.Length(254,varying=true))
    /** Database column CITY DBType(VARCHAR), Length(254,true) */
    val city: Column[String] = column[String]("CITY", O.Length(254,varying=true))
    /** Database column STATE DBType(VARCHAR), Length(254,true) */
    val state: Column[String] = column[String]("STATE", O.Length(254,varying=true))
    /** Database column ZIP DBType(VARCHAR), Length(254,true) */
    val zip: Column[String] = column[String]("ZIP", O.Length(254,varying=true))
  }
  /** Collection-like TableQuery object for table Suppliers */
  lazy val Suppliers = new TableQuery(tag => new Suppliers(tag))
}