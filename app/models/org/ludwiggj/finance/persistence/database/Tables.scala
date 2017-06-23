package models.org.ludwiggj.finance.persistence.database

import java.sql.Timestamp

import models.org.ludwiggj.finance.domain.TransactionType.aTransactionType
import models.org.ludwiggj.finance.domain.{FundName, TransactionType}
import org.joda.time.LocalDate

import scala.slick.lifted.MappedTo

object PKs {

  final case class PK[A](value: Long) extends AnyVal with MappedTo[Long] with Ordered[PK[A]] {
    def compare(that: PK[A]): Int = value.compare(that.value)
  }

}

object Tables extends {
  val profile = scala.slick.driver.H2Driver
} with Tables

// Slick data model trait for extension, choice of backend or usage in the cake pattern.
trait Tables {

  import PKs.PK

  val profile: scala.slick.driver.JdbcProfile

  import profile.simple._

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

  implicit val fundNameMapper = MappedColumnType.base[FundName, String](
    fn => fn.name,
    s => FundName(s)
  )

  case class FundRow(
                      id: PK[FundTable],
                      name: FundName
                    )

  class FundTable(_tableTag: Tag) extends Table[FundRow](_tableTag, "FUNDS") {
    val id = column[PK[FundTable]]("ID", O.AutoInc, O.PrimaryKey)
    val name = column[FundName]("NAME", O.Length(254, varying = true))

    def * = (id, name) <> (FundRow.tupled, FundRow.unapply)

    val index1 = index("NAME", name, unique = true)
  }

  lazy val Funds = new TableQuery(tag => new FundTable(tag))

  // ---------------
  // PRICES
  // ---------------

  case class PriceRow(
                       fundId: PK[FundTable],
                       date: LocalDate,
                       price: BigDecimal
                     )

  class PriceTable(_tableTag: Tag) extends Table[PriceRow](_tableTag, "PRICES") {
    val fundId = column[PK[FundTable]]("FUND_ID")
    val date = column[LocalDate]("PRICE_DATE")
    val price = column[BigDecimal]("PRICE")

    def * = (fundId, date, price) <> (PriceRow.tupled, PriceRow.unapply)

    // Primary key
    val pk = primaryKey("PRICES_PK", (fundId, date))

    // Foreign key referencing Funds
    lazy val fundsFk = foreignKey(
      "PRICES_FUNDS_FK", fundId, Funds
    )(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  lazy val Prices = new TableQuery(tag => new PriceTable(tag))

  // ---------------
  // TRANSACTIONS
  // ---------------

  implicit val transactionTypeMapper = MappedColumnType.base[TransactionType, String](
    tt => tt.name,
    s => aTransactionType(s)
  )

  case class TransactionRow(
                             fundId: PK[FundTable],
                             userId: PK[UserTable],
                             date: LocalDate,
                             description: TransactionType,
                             amountIn: Option[BigDecimal] = None,
                             amountOut: Option[BigDecimal] = None,
                             priceDate: LocalDate,
                             units: BigDecimal
                           )

  class TransactionTable(_tableTag: Tag) extends Table[TransactionRow](_tableTag, "TRANSACTIONS") {
    val id = column[PK[TransactionTable]]("ID", O.AutoInc, O.PrimaryKey)
    val fundId = column[PK[FundTable]]("FUND_ID")
    val userId = column[PK[UserTable]]("USER_ID")
    val date = column[LocalDate]("TRANSACTION_DATE")
    val description = column[TransactionType]("DESCRIPTION", O.Length(254, varying = true))
    val amountIn = column[Option[BigDecimal]]("AMOUNT_IN")
    val amountOut = column[Option[BigDecimal]]("AMOUNT_OUT")
    val priceDate = column[LocalDate]("PRICE_DATE")
    val units = column[BigDecimal]("UNITS")

    def * = (
      fundId, userId, date, description, amountIn, amountOut, priceDate, units
    ) <> (TransactionRow.tupled, TransactionRow.unapply)

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

  case class UserRow(
                      id: PK[UserTable],
                      name: String,
                      password: Option[String] = None
                    )

  class UserTable(_tableTag: Tag) extends Table[UserRow](_tableTag, "USERS") {
    val id = column[PK[UserTable]]("ID", O.AutoInc, O.PrimaryKey)
    val name = column[String]("NAME", O.Length(254, varying = true))
    val password = column[Option[String]]("PASSWORD", O.Length(254, varying = true), O.Default(None))

    def * = (id, name, password) <> (UserRow.tupled, UserRow.unapply)

    val index1 = index("NAME", name, unique = true)
  }

  lazy val Users = new TableQuery(tag => new UserTable(tag))
}