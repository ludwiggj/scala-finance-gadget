package models.org.ludwiggj.finance.persistence.database

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import models.org.ludwiggj.finance.domain.{Transaction, Holding}
import scala.slick.driver.MySQLDriver.simple._
import Tables.{HoldingsRow, Holdings, Transactions}
import play.api.db.DB
import play.api.Play.current
import models.org.ludwiggj.finance.persistence.database.UsersDatabase.usersRowWrapper

class DatabasePersister {

  implicit lazy val db = Database.forDataSource(DB.getDataSource("finance"))

  def persistHoldings(accountName: String, holdingsToPersist: List[Holding]) {
    val holdings: TableQuery[Holdings] = TableQuery[Holdings]

    db.withSession {
      implicit session =>

        val userId = UsersDatabase().getOrInsert(accountName)

        def persistHolding(holding: Holding) {

          def insertHolding(fundId: Long) {
            try {
              holdings += HoldingsRow(fundId, userId, holding.units, holding.priceDateAsSqlDate)
            } catch {
              case ex: MySQLIntegrityConstraintViolationException =>
                println(s"Holding: ${ex.getMessage}")
            }
          }

          PricesDatabase().insert(holding.price)
          insertHolding(FundsDatabase().get(holding.name).get.id)
        }

        for (holdingToPersist <- holdingsToPersist) {
          persistHolding(holdingToPersist)
        }
    }
  }

  def persistTransactions(accountName: String, transactionsToPersist: List[Transaction]) {
    val transactions: TableQuery[Transactions] = TableQuery[Transactions]

    db.withSession {
      implicit session =>

        val userId = UsersDatabase().getOrInsert(accountName)

        def persistTransaction(transaction: Transaction) {

          def insertTransaction(fundId: Long) {
            try {
              if (transaction.in.isDefined) {
                transactions.map(
                  t => (t.fundId, t.userId, t.transactionDate, t.description, t.amountIn, t.priceDate, t.units)
                ) +=(
                  fundId, userId, transaction.dateAsSqlDate, transaction.description, transaction.in.get,
                  transaction.priceDateAsSqlDate, transaction.units
                  )
              }
              if (transaction.out.isDefined) {
                transactions.map(
                  t => (t.fundId, t.userId, t.transactionDate, t.description, t.amountOut, t.priceDate, t.units)
                ) +=(
                  fundId, userId, transaction.dateAsSqlDate, transaction.description, transaction.out.get,
                  transaction.priceDateAsSqlDate, transaction.units
                  )
              }
            } catch {
              case ex: MySQLIntegrityConstraintViolationException =>
                println(s"Transaction: ${ex.getMessage}")
            }
          }

          PricesDatabase().insert((transaction.price))
          insertTransaction(FundsDatabase().get(transaction.holdingName).get.id)
        }

        for (transactionToPersist <- transactionsToPersist) {
          persistTransaction(transactionToPersist)
        }
    }
  }
}