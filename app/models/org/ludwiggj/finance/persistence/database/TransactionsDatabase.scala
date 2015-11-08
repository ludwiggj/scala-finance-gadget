package models.org.ludwiggj.finance.persistence.database

import java.sql.Date
import models.org.ludwiggj.finance.domain.{Price, Transaction}
import scala.collection.immutable.ListMap
import scala.slick.driver.MySQLDriver.simple._
import play.api.db.DB
import play.api.Play.current
import models.org.ludwiggj.finance.persistence.database.UsersDatabase.stringToUsersRow
import models.org.ludwiggj.finance.persistence.database.Tables._
import scala.language.implicitConversions
import TransactionsDatabase.InvestmentRegular

class TransactionsDatabase private {

  implicit lazy val db = Database.forDataSource(DB.getDataSource("finance"))

  def insert(transaction: Transaction) {

    db.withSession {
      implicit session =>

        if (!get().contains(transaction)) {

          val userId = UsersDatabase().getOrInsert(transaction.userName)

          def insert(fundId: Long) {
            Transactions += TransactionsRow(
              fundId, userId, transaction.date, transaction.description, transaction.in, transaction.out,
              transaction.priceDate, transaction.units
            )
          }

          PricesDatabase().insert(transaction.price)

          FundsDatabase().get(transaction.fundName) match {
            case Some(fundsRow) => insert(fundsRow.id)
            case _ => println(s"Could not insert Transaction: fund ${transaction.fundName} not found")
          }
        }
    }
  }

  def insert(transactions: List[Transaction]): Unit = {
    for (transaction <- transactions) {
      insert(transaction)
    }
  }

  implicit class TransactionExtension(q: Query[Transactions, TransactionsRow, Seq]) {
    def withFunds = {
      q.join(Funds).on((t, f) => t.fundId === f.id)
    }

    def withFundsAndPricesAndUser = {
      q.join(Prices).on((t, p) => t.fundId === p.fundId && t.priceDate === p.date)
        .join(Funds).on((h_p, f) => h_p._1.fundId === f.id)
        .join(Users).on((h_p_f, u) => h_p_f._1._1.userId === u.id)
    }
  }

  implicit def bigDecimalToOption(value: BigDecimal) = {
    if (value == 0) None else Some(value)
  }

  implicit def optionToBigDecimal(value: Option[BigDecimal]): BigDecimal = {
    if (value.isDefined) value.get else 0
  }

  implicit def asListOfTransactions(q: Query[(((Transactions, Prices), Funds), Users),
    (((TransactionsRow, PricesRow), FundsRow), UsersRow), Seq]): List[Transaction] = {
    db.withSession {
      implicit session =>
        q.list map {
          case (((TransactionsRow(_, _, date, description, amountIn, amountOut, priceDate, units),
          PricesRow(_, _, price)), FundsRow(_, fundName)), UsersRow(_, userName)) =>
            Transaction(userName, date, description, amountIn, amountOut, Price(fundName, priceDate, price), units)
        }
    }
  }

  def get(): List[Transaction] = {
    db.withSession {
      implicit session =>
        Transactions.withFundsAndPricesAndUser
    }
  }

  def getRegularInvestmentDates(): List[Date] = {
    db.withSession {
      implicit session =>
        Transactions
          .filter { tx => tx.description === InvestmentRegular }
          .map {
          _.date
        }.sorted.list.reverse.distinct
    }
  }

  def getTransactionsUpToAndIncluding(dateOfInterest: Date): TransactionMap = {
    db.withSession {
      implicit session =>
        val transactionsOfInterest = (for {
          t <- Transactions.filter {
            _.date <= dateOfInterest
          }
          f <- Funds if f.id === t.fundId
          u <- Users if u.id === t.userId
          p <- Prices if t.fundId === p.fundId && t.priceDate === p.date
        } yield (u.name, f.name, f.id, p, t)).run.groupBy(t => (t._1, t._2))

        val sortedTransactionsOfInterest = ListMap(transactionsOfInterest.toSeq.sortBy(k => k._1): _*)

        def amountOption(amount: BigDecimal) = if (amount == 0.0) None else Some(amount)

        sortedTransactionsOfInterest.mapValues(rows => {
          val (_, _, fundId, _, _) = rows.head
          val latestPrice = PricesDatabase().latestPrices(dateOfInterest)(fundId)

          val txs = for {
            (userName, fundName, fundId, priceRow, tx) <- rows
          } yield Transaction(userName, tx.date, tx.description, amountOption(tx.amountIn),
              amountOption(tx.amountOut), Price(fundName, priceRow.date, priceRow.price), tx.units)
          (txs, latestPrice)
        }
        )
    }
  }
}

// Note: declaring a Transactions companion object would break the <> mapping.
object TransactionsDatabase {
  val InvestmentRegular = "Investment Regular"
  val InvestmentLumpSum = "Investment Lump Sum"
  val DividendReinvestment = "Dividend Reinvestment"
  val SaleForRegularPayment = "Sale for Regular Payment"
  val UnitShareConversionIn = "Unit/Share Conversion +"
  val UnitShareConversionOut = "Unit/Share Conversion -"

  def apply() = new TransactionsDatabase()
}