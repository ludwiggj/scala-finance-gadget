package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.{FinanceDate, Price, Transaction}
import scala.slick.driver.MySQLDriver.simple._
import play.api.db.DB
import play.api.Play.current
import models.org.ludwiggj.finance.persistence.database.UsersDatabase.usersRowWrapper
import models.org.ludwiggj.finance.persistence.database.Tables._
import models.org.ludwiggj.finance.asSqlDate
import scala.language.implicitConversions

class TransactionsDatabase {

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

          FundsDatabase().get(transaction.holdingName) match {
            case Some(fundsRow) => insert(fundsRow.id)
            case _ => println(s"Could not insert Transaction: fund ${transaction.holdingName} not found")
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
      q.join(Prices).on((t, p) => t.fundId === p.fundId && t.priceDate === p.priceDate)
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
          case (((TransactionsRow(_, _, transactionDate, description, amountIn, amountOut, priceDate, units),
          PricesRow(_, _, price)), FundsRow(_, fundName)), UsersRow(_, userName)) =>
            Transaction(userName, FinanceDate(transactionDate), description, amountIn, amountOut,
              Price(fundName, priceDate, price), units)
        }
    }
  }

  def get(): List[Transaction] = {
    db.withSession {
      implicit session =>
        Transactions.withFundsAndPricesAndUser
    }
  }
}

// Note: declaring a Transactions companion object would break the <> mapping.
object TransactionsDatabase {
  def apply() = {
    new TransactionsDatabase()
  }
}