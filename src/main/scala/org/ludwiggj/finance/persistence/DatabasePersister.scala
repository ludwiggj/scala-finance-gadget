package org.ludwiggj.finance.persistence

import java.sql.Date

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import org.ludwiggj.finance.database.Tables
import org.ludwiggj.finance.database.Tables.{Funds, Holdings, Users, Prices, Transactions}
import org.ludwiggj.finance.domain.{Transaction, Holding}
import scala.slick.driver.MySQLDriver.simple._

class DatabasePersister {

  val db = Database.forConfig("db")

  def getOrInsertUser(name: String): Long = {
    val users: TableQuery[Users] = TableQuery[Users]
    db.withSession {
      implicit session =>
        val filter = users.filter {
          _.name === name
        }
        if (!filter.exists.run) {
          import Tables.UsersRow
          (users returning users.map(_.id)) += (0L, name)
        } else {
          filter.first._1
        }
    }
  }

  def getOrInsertFund(name: String): Long = {
    val funds: TableQuery[Funds] = TableQuery[Funds]
    db.withSession {
      implicit session =>
        val filter = funds.filter {
          _.name === name
        }
        if (!filter.exists.run) {
          ((funds returning funds.map(_.id)) +=(0L, name))
        } else {
          filter.first._1
        }
    }
  }

  def insertPrice(fundId: Long, priceDate: Date, priceInPounds: BigDecimal) {
    val prices: TableQuery[Prices] = TableQuery[Prices]
    db.withSession {
      implicit session =>
        try {
          prices +=(fundId, priceDate, priceInPounds)
        } catch {
          case ex: MySQLIntegrityConstraintViolationException =>
            println(s"Price: ${ex.getMessage}")
        }
    }
  }

  def persistHoldings(accountName: String, holdingsToPersist: List[Holding]) {
    val holdings: TableQuery[Holdings] = TableQuery[Holdings]

    db.withSession {
      implicit session =>

        val userId = getOrInsertUser(accountName)

        def persistHolding(holding: Holding) {

          def insertHolding(fundId: Long) {
            try {
              holdings +=(fundId, userId, holding.units, holding.priceDateAsSqlDate)
            } catch {
              case ex: MySQLIntegrityConstraintViolationException =>
                println(s"Holding: ${ex.getMessage}")
            }
          }

          val fundId = getOrInsertFund(holding.name)
          insertPrice(fundId, holding.priceDateAsSqlDate, holding.priceInPounds)
          insertHolding(fundId)
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

        val userId = getOrInsertUser(accountName)

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

          val fundId = getOrInsertFund(transaction.holdingName)
          insertPrice(fundId, transaction.priceDateAsSqlDate, transaction.priceInPounds)
          insertTransaction(fundId)
        }

        for (transactionToPersist <- transactionsToPersist) {
          persistTransaction(transactionToPersist)
        }
    }
  }
}