package org.ludwiggj.finance.database

import scala.slick.lifted.ProvenShape

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

  /** DDL for all tables. Call .create to execute. */
  lazy val ddl = Changelog.ddl ++ Funds.ddl ++ Holdings.ddl ++ Prices.ddl ++ Users.ddl

  /** Row type of table Changelog */
  type ChangelogRow = (Long, java.sql.Timestamp, String, String)
  /** Constructor for ChangelogRow providing default values if available in the database schema. */
  def ChangelogRow(changeNumber: Long, completeDt: java.sql.Timestamp, appliedBy: String, description: String): ChangelogRow = {
    (changeNumber, completeDt, appliedBy, description)
  }
  /** Table description of table changelog. Objects of this class serve as prototypes for rows in queries. */
  class Changelog(_tableTag: Tag) extends Table[ChangelogRow](_tableTag, "changelog") {
    def * = (changeNumber, completeDt, appliedBy, description)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (changeNumber.?, completeDt.?, appliedBy.?, description.?).shaped.<>({r=>import r._; _1.map(_=> ChangelogRow(_1.get, _2.get, _3.get, _4.get))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column change_number DBType(BIGINT), PrimaryKey */
    val changeNumber: Column[Long] = column[Long]("change_number", O.PrimaryKey)
    /** Database column complete_dt DBType(TIMESTAMP) */
    val completeDt: Column[java.sql.Timestamp] = column[java.sql.Timestamp]("complete_dt")
    /** Database column applied_by DBType(VARCHAR), Length(100,true) */
    val appliedBy: Column[String] = column[String]("applied_by", O.Length(100,varying=true))
    /** Database column description DBType(VARCHAR), Length(500,true) */
    val description: Column[String] = column[String]("description", O.Length(500,varying=true))
  }
  /** Collection-like TableQuery object for table Changelog */
  lazy val Changelog = new TableQuery(tag => new Changelog(tag))

  /** Row type of table Funds */
  type FundsRow = (Long, String)
  /** Constructor for FundsRow providing default values if available in the database schema. */
  def FundsRow(id: Long, name: String): FundsRow = {
    (id, name)
  }
  /** Table description of table FUNDS. Objects of this class serve as prototypes for rows in queries. */
  class Funds(_tableTag: Tag) extends Table[FundsRow](_tableTag, "FUNDS") {
    def * = (id, name)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, name.?).shaped.<>({r=>import r._; _1.map(_=> FundsRow(_1.get, _2.get))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    /** Database column NAME DBType(VARCHAR), Length(254,true) */
    val name: Column[String] = column[String]("NAME", O.Length(254,varying=true))

    /** Uniqueness Index over (name) (database name NAME) */
    val index1 = index("NAME", name, unique=true)
  }
  /** Collection-like TableQuery object for table Funds */
  lazy val Funds = new TableQuery(tag => new Funds(tag))

  /** Row type of table Holdings */
  type HoldingsRow = (Long, Long, Double, java.sql.Date)
  /** Constructor for HoldingsRow providing default values if available in the database schema. */
  def HoldingsRow(fundId: Long, userId: Long, units: Double, dateOfRecord: java.sql.Date): HoldingsRow = {
    (fundId, userId, units, dateOfRecord)
  }
  /** Table description of table HOLDINGS. Objects of this class serve as prototypes for rows in queries. */
  class Holdings(_tableTag: Tag) extends Table[HoldingsRow](_tableTag, "HOLDINGS") {
    def * = (fundId, userId, units, dateOfRecord)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (fundId.?, userId.?, units.?, dateOfRecord.?).shaped.<>({r=>import r._; _1.map(_=> HoldingsRow(_1.get, _2.get, _3.get, _4.get))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column FUND_ID DBType(BIGINT) */
    val fundId: Column[Long] = column[Long]("FUND_ID")
    /** Database column USER_ID DBType(BIGINT) */
    val userId: Column[Long] = column[Long]("USER_ID")
    /** Database column UNITS DBType(DOUBLE) */
    val units: Column[Double] = column[Double]("UNITS")
    /** Database column DATE_OF_RECORD DBType(DATE) */
    val dateOfRecord: Column[java.sql.Date] = column[java.sql.Date]("DATE_OF_RECORD")

    /** Primary key of Holdings (database name HOLDINGS_PK) */
    val pk = primaryKey("HOLDINGS_PK", (fundId, userId, dateOfRecord))

    /** Foreign key referencing Funds (database name HOLDINGS_FUND_FK) */
    lazy val fundsFk = foreignKey("HOLDINGS_FUND_FK", fundId, Funds)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing Users (database name HOLDINGS_USER_FK) */
    lazy val usersFk = foreignKey("HOLDINGS_USER_FK", userId, Users)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Holdings */
  lazy val Holdings = new TableQuery(tag => new Holdings(tag))

  /** Row type of table Prices */
  type PricesRow = (Long, java.sql.Date, Double)
  /** Constructor for PricesRow providing default values if available in the database schema. */
  def PricesRow(fundId: Long, priceDate: java.sql.Date, priceInPence: Double): PricesRow = {
    (fundId, priceDate, priceInPence)
  }
  /** Table description of table PRICES. Objects of this class serve as prototypes for rows in queries. */
  class Prices(_tableTag: Tag) extends Table[PricesRow](_tableTag, "PRICES") {
    def * = (fundId, priceDate, priceInPence)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (fundId.?, priceDate.?, priceInPence.?).shaped.<>({r=>import r._; _1.map(_=> PricesRow(_1.get, _2.get, _3.get))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column FUND_ID DBType(BIGINT) */
    val fundId: Column[Long] = column[Long]("FUND_ID")
    /** Database column PRICE_DATE DBType(DATE) */
    val priceDate: Column[java.sql.Date] = column[java.sql.Date]("PRICE_DATE")
    /** Database column PRICE_IN_PENCE DBType(DOUBLE) */
    val priceInPence: Column[Double] = column[Double]("PRICE_IN_PENCE")

    /** Primary key of Prices (database name PRICES_PK) */
    val pk = primaryKey("PRICES_PK", (fundId, priceDate))

    /** Foreign key referencing Funds (database name PRICES_FUND_FK) */
    lazy val fundsFk = foreignKey("PRICES_FUND_FK", fundId, Funds)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Prices */
  lazy val Prices = new TableQuery(tag => new Prices(tag))

  /** Row type of table Users */
  type UsersRow = (Long, String)
  /** Constructor for UsersRow providing default values if available in the database schema. */
  def UsersRow(id: Long, name: String): UsersRow = {
    (id, name)
  }
  /** Table description of table USERS. Objects of this class serve as prototypes for rows in queries. */
  class Users(_tableTag: Tag) extends Table[UsersRow](_tableTag, "USERS") {
    def * : ProvenShape[UsersRow] = (id, name)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, name.?).shaped.<>({r=>import r._; _1.map(_=> UsersRow(_1.get, _2.get))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    /** Database column NAME DBType(VARCHAR), Length(254,true) */
    val name: Column[String] = column[String]("NAME", O.Length(254,varying=true))

    /** Uniqueness Index over (name) (database name NAME) */
    val index1 = index("NAME", name, unique=true)
  }
  /** Collection-like TableQuery object for table Users */
  lazy val Users = new TableQuery(tag => new Users(tag))
}