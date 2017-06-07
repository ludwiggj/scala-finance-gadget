package models.org.ludwiggj.finance.persistence.database

import java.sql.Timestamp

import org.joda.time.LocalDate

import scala.slick.lifted.MappedTo

object PKs {
  final case class PK[A](value: Long) extends AnyVal with MappedTo[Long]
}

object Tables extends {
  val profile = scala.slick.driver.MySQLDriver
} with Tables

// Slick data model trait for extension, choice of backend or usage in the cake pattern.
trait Tables {
  import PKs.PK

  val profile: scala.slick.driver.JdbcProfile

  import profile.simple._

  // Graeme, added to handle BigDecimal bug
  import models.org.ludwiggj.finance.domain._

  /** DDL for all tables. Call .create to execute. */
  lazy val ddl = Funds.ddl ++ Prices.ddl ++ Transactions.ddl ++ Users.ddl

  // Map joda LocalDate into sql Timestamp
  implicit val jodaDateTimeType = MappedColumnType.base[LocalDate, Timestamp](
    ld => new Timestamp(ld.toDateTimeAtStartOfDay.getMillis),
    ts => new LocalDate(ts.getTime)
  )

  // ---------------
  // FUNDS
  // ---------------

  // Entity class storing rows of table Funds
  case class FundsRow(
                       id: PK[FundTable],
                       name: String
                     )

  // Funds table
  class FundTable(_tableTag: Tag) extends Table[FundsRow](_tableTag, "FUNDS") {
    val id = column[PK[FundTable]]("ID", O.AutoInc, O.PrimaryKey)
    val name = column[String]("NAME", O.Length(254, varying = true))

    def * = (id, name) <> (FundsRow.tupled, FundsRow.unapply)

    val index1 = index("NAME", name, unique = true)
  }

  lazy val Funds = new TableQuery(tag => new FundTable(tag))

  // ---------------
  // PRICES
  // ---------------

  // Entity class storing rows of table Prices
  case class PricesRow(
                        fundId: PK[FundTable],
                        date: LocalDate,
                        price: BigDecimal
                      )

  // Prices table
  class Prices(_tableTag: Tag) extends Table[PricesRow](_tableTag, "PRICES") {
    val fundId = column[PK[FundTable]]("FUND_ID")
    val date = column[LocalDate]("PRICE_DATE")
    val price = column[BigDecimal]("PRICE")

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
    tt => tt.name,
    s => TransactionType.fromString(s)
  )

  // Entity class storing rows of table Transactions
  case class TransactionsRow(
                              fundId: PK[FundTable],
                              userId: Long,
                              date: LocalDate,
                              description: TransactionType,
                              amountIn: Option[BigDecimal] = None,
                              amountOut: Option[BigDecimal] = None,
                              priceDate: LocalDate,
                              units: BigDecimal
                            )

  // Transactions table
  class TransactionTable(_tableTag: Tag) extends Table[TransactionsRow](_tableTag, "TRANSACTIONS") {
    val id = column[PK[TransactionTable]]("ID", O.AutoInc, O.PrimaryKey)
    val fundId = column[PK[FundTable]]("FUND_ID")
    val userId = column[Long]("USER_ID")
    val date = column[LocalDate]("TRANSACTION_DATE")
    val description = column[TransactionType]("DESCRIPTION", O.Length(254, varying = true))
    val amountIn = column[Option[BigDecimal]]("AMOUNT_IN")
    val amountOut = column[Option[BigDecimal]]("AMOUNT_OUT")
    val priceDate = column[LocalDate]("PRICE_DATE")
    val units = column[BigDecimal]("UNITS")

    def * = (
      fundId, userId, date, description, amountIn, amountOut, priceDate, units
    ) <> (TransactionsRow.tupled, TransactionsRow.unapply)

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

  lazy val Transactions = new TableQuery(tag => new TransactionTable(tag))

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
    val id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    val name = column[String]("NAME", O.Length(254, varying = true))
    val password = column[Option[String]]("PASSWORD", O.Length(254, varying = true), O.Default(None))

    def * = (id, name, password) <> (UsersRow.tupled, UsersRow.unapply)

    val index1 = index("NAME", name, unique = true)
  }

  lazy val Users = new TableQuery(tag => new Users(tag))
}