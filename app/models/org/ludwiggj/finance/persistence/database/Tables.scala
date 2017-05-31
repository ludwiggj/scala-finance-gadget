package models.org.ludwiggj.finance.persistence.database

import java.sql.Date

import models.org.ludwiggj.finance.domain.TransactionType.TransactionType

object Tables extends {
  val profile = scala.slick.driver.MySQLDriver
} with Tables

// Slick data model trait for extension, choice of backend or usage in the cake pattern.
trait Tables {
  val profile: scala.slick.driver.JdbcProfile

  import profile.simple._

  // Graeme, added to handle BigDecimal bug
  import models.org.ludwiggj.finance.domain._

  /** DDL for all tables. Call .create to execute. */
  lazy val ddl = Funds.ddl ++ Prices.ddl ++ Transactions.ddl ++ Users.ddl

  // ---------------
  // FUNDS
  // ---------------

  // Entity class storing rows of table Funds
  case class FundsRow(
                       id: Long,
                       name: String
                     )

  // Funds table
  class Funds(_tableTag: Tag) extends Table[FundsRow](_tableTag, "FUNDS") {
    val id: Column[Long] = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    val name: Column[String] = column[String]("NAME", O.Length(254, varying = true))

    def * = (id, name) <> (FundsRow.tupled, FundsRow.unapply)

    val index1 = index("NAME", name, unique = true)
  }

  lazy val Funds = new TableQuery(tag => new Funds(tag))

  // ---------------
  // PRICES
  // ---------------

  // Entity class storing rows of table Prices
  case class PricesRow(
                        fundId: Long,
                        date: Date,
                        price: BigDecimal
                      )

  // Prices table
  class Prices(_tableTag: Tag) extends Table[PricesRow](_tableTag, "PRICES") {
    val fundId: Column[Long] = column[Long]("FUND_ID")
    val date: Column[Date] = column[Date]("PRICE_DATE")
    val price: Column[BigDecimal] = column[BigDecimal]("PRICE")

    def * = (fundId, date, price) <> (PricesRow.tupled, PricesRow.unapply)

    // Primary key
    val pk = primaryKey("PRICES_PK", (fundId, date))

    // Foreign key referencing Funds
    lazy val fundsFk = foreignKey(
      "PRICES_FUNDS_FK", fundId, Funds
    )(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  lazy val Prices = new TableQuery(tag => new Prices(tag))

  // ---------------
  // TRANSACTIONS
  // ---------------

  implicit val transactionTypeMapper = MappedColumnType.base[TransactionType, String](
    e => e.toString,
    s => TransactionType.withName(s)
  )

  // Entity class storing rows of table Transactions
  case class TransactionsRow(
                              fundId: Long,
                              userId: Long,
                              date: Date,
                              description: TransactionType,
                              amountIn: BigDecimal = "0.0000",
                              amountOut: BigDecimal = "0.0000",
                              priceDate: Date,
                              units: BigDecimal
                            )

  // Transactions table
  class Transactions(_tableTag: Tag) extends Table[TransactionsRow](_tableTag, "TRANSACTIONS") {
    val fundId: Column[Long] = column[Long]("FUND_ID")
    val userId: Column[Long] = column[Long]("USER_ID")
    val date: Column[Date] = column[Date]("TRANSACTION_DATE")
    val description: Column[TransactionType] = column[TransactionType]("DESCRIPTION", O.Length(254, varying = true))
    val amountIn: Column[BigDecimal] = column[BigDecimal]("AMOUNT_IN", O.Default("0.0000"))
    val amountOut: Column[BigDecimal] = column[BigDecimal]("AMOUNT_OUT", O.Default("0.0000"))
    val priceDate: Column[Date] = column[Date]("PRICE_DATE")
    val units: Column[BigDecimal] = column[BigDecimal]("UNITS")

    def * = (
      fundId, userId, date, description, amountIn, amountOut, priceDate, units
    ) <> (TransactionsRow.tupled, TransactionsRow.unapply)

    // Primary key
    val pk = primaryKey("TRANSACTIONS_PK", (fundId, userId, date, description, amountIn, amountOut, priceDate, units))

    // Foreign keys
    lazy val fundsFk = foreignKey(
      "TRANSACTIONS_FUNDS_FK", fundId, Funds
    )(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

    lazy val pricesFk = foreignKey(
      "TRANSACTIONS_PRICES_FK", (fundId, priceDate), Prices
    )(r => (r.fundId, r.date), onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

    lazy val usersFk = foreignKey(
      "TRANSACTIONS_USERS_FK", userId, Users
    )(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  lazy val Transactions = new TableQuery(tag => new Transactions(tag))

  // ---------------
  // USERS
  // ---------------

  // Entity class storing rows of table Users
  case class UsersRow(
                       id: Long,
                       name: String,
                       password: Option[String] = None
                     )

  // Users Table
  class Users(_tableTag: Tag) extends Table[UsersRow](_tableTag, "USERS") {
    val id: Column[Long] = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    val name: Column[String] = column[String]("NAME", O.Length(254, varying = true))
    val password: Column[Option[String]] = column[Option[String]]("PASSWORD", O.Length(254, varying = true), O.Default(None))

    def * = (id, name, password) <> (UsersRow.tupled, UsersRow.unapply)

    val index1 = index("NAME", name, unique = true)
  }

  lazy val Users = new TableQuery(tag => new Users(tag))
}