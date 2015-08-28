package models.org.ludwiggj.finance.persistence.database

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import models.org.ludwiggj.finance.domain.{Transaction, Price, Holding}
import scala.slick.driver.MySQLDriver.simple._
import Tables.{FundsRow, HoldingsRow, PricesRow, Funds, Prices, Holdings, Transactions}
import play.api.db.DB
import play.api.Play.current

class DatabasePersister {

  implicit lazy val db = Database.forDataSource(DB.getDataSource("finance"))

  def getOrInsertFund(name: String): Long = {
    val funds: TableQuery[Funds] = TableQuery[Funds]
    db.withSession {
      implicit session =>
        val filter = funds.filter {
          _.name === name
        }
        if (!filter.exists.run) {
          ((funds returning funds.map(_.id)) += FundsRow(0L, name))
        } else {
          filter.first.id
        }
    }
  }

  def insertPrice(fundId: Long, price: Price) {
    val prices: TableQuery[Prices] = TableQuery[Prices]
    db.withSession {
      implicit session =>
        try {
          prices += PricesRow(fundId, price.dateAsSqlDate, price.inPounds)
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

        val userId = UserDatabase().getOrInsert(accountName)

        def persistHolding(holding: Holding) {

          def insertHolding(fundId: Long) {
            try {
              holdings += HoldingsRow(fundId, userId, holding.units, holding.priceDateAsSqlDate)
            } catch {
              case ex: MySQLIntegrityConstraintViolationException =>
                println(s"Holding: ${ex.getMessage}")
            }
          }

          val fundId = getOrInsertFund(holding.name)
          insertPrice(fundId, holding.price)
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

        val userId = UserDatabase().getOrInsert(accountName)

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
          insertPrice(fundId, transaction.price)
          insertTransaction(fundId)
        }

        for (transactionToPersist <- transactionsToPersist) {
          persistTransaction(transactionToPersist)
        }
    }
  }

  def persistPrices(pricesToPersist: List[Price]) {
    val prices: TableQuery[Prices] = TableQuery[Prices]

    db.withSession {
      implicit session =>

        def persistPrice(price: Price) {
          val fundId = getOrInsertFund(price.holdingName)
          insertPrice(fundId, price)
        }

        for (priceToPersist <- pricesToPersist) {
          persistPrice(priceToPersist)
        }
    }
  }
}