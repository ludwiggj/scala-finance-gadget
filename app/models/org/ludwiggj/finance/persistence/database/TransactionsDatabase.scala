package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.{FinanceDate, Price, Transaction}
import scala.slick.driver.MySQLDriver.simple._
import play.api.db.DB
import play.api.Play.current
import models.org.ludwiggj.finance.persistence.database.UsersDatabase.usersRowWrapper
import models.org.ludwiggj.finance.persistence.database.Tables._
import scala.language.implicitConversions

class TransactionsDatabase {

  implicit lazy val db = Database.forDataSource(DB.getDataSource("finance"))

  def insert(accountName: String, transaction: Transaction) {

    db.withSession {
      implicit session =>

        if (!get().contains(transaction)) {

          val userId = UsersDatabase().getOrInsert(accountName)

          def insert(fundId: Long) {
            Transactions += TransactionsRow(
              fundId, userId, transaction.dateAsSqlDate, transaction.description, transaction.in, transaction.out,
              transaction.priceDateAsSqlDate, transaction.units
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

  def insert(accountName: String, transactions: List[Transaction]): Unit = {
    for (transaction <- transactions) {
      insert(accountName, transaction)
    }
  }

  implicit class TransactionExtension(q: Query[Transactions, TransactionsRow, Seq]) {
    def withFunds = {
      q.join(Funds).on((t, f) => t.fundId === f.id)
    }

    def withFundsAndPrices = {
      q.join(Prices).on((t, p) => t.fundId === p.fundId && t.priceDate === p.priceDate)
        .join(Funds).on((h_p, f) => h_p._1.fundId === f.id)
    }
  }

  implicit def bigDecimalToOption(value: BigDecimal) = {
    if (value == 0) None else Some(value)
  }

  implicit def optionToBigDecimal(value: Option[BigDecimal]): BigDecimal = {
    if (value.isDefined) value.get else 0
  }

  implicit def asListOfTransactions(q: Query[((Transactions, Prices), Funds), ((TransactionsRow, PricesRow), FundsRow), Seq]): List[Transaction] = {
    db.withSession {
      implicit session =>
        q.list map {
          case ((TransactionsRow(_, _, transactionDate, description, amountIn, amountOut, priceDate, units),
          PricesRow(_, _, price)), FundsRow(_, fundName)) =>
            Transaction(FinanceDate(transactionDate), description, amountIn, amountOut,
              Price(fundName, priceDate, price), units)
        }
    }
  }

  def get(): List[Transaction] = {
    db.withSession {
      implicit session =>
        Transactions.withFundsAndPrices
    }
  }
}

// Note: declaring a Transactions companion object would break the <> mapping.
object TransactionsDatabase {
  def apply() = {
    new TransactionsDatabase()
  }
}