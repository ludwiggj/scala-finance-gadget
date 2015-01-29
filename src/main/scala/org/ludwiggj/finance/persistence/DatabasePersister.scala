package org.ludwiggj.finance.persistence

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import org.ludwiggj.finance.database.Tables.{Funds, Holdings, HoldingsRow, Users, Prices, PricesRow}
import org.ludwiggj.finance.domain.Holding
import scala.slick.driver.MySQLDriver.simple._

class DatabasePersister {
  val users: TableQuery[Users] = TableQuery[Users]
  val funds: TableQuery[Funds] = TableQuery[Funds]
  val prices: TableQuery[Prices] = TableQuery[Prices]
  val holdings: TableQuery[Holdings] = TableQuery[Holdings]
  val db = Database.forConfig("db")

  def persist(accountName: String, holdingsToPersist: List[Holding]) {
    db.withSession {
      implicit session =>
        def persistHolding(userId: Long, holding: Holding) {
          def insertOrCreateFund(name: String): Long = {
            val filter = funds.filter { _.name === name }
            if (!filter.exists.run) {
              ((funds returning funds.map(_.id)) += (0L, name))
            } else {
              filter.first._1
            }
          }

          def insertHolding(holding: HoldingsRow) {
            try {
              holdings += holding
            } catch {
              case ex: MySQLIntegrityConstraintViolationException =>
                println(s"Holding: ${ex.getMessage}")
            }
          }

          def insertPrice(price: PricesRow) {
            try {
              prices += price
            } catch {
              case ex: MySQLIntegrityConstraintViolationException =>
                println(s"Price: ${ex.getMessage}")
            }
          }

          val fundId = insertOrCreateFund(holding.name)
          insertHolding(fundId, userId, holding.units.toDouble, holding.dateAsSqlDate)
          insertPrice(fundId, holding.dateAsSqlDate, holding.priceInPence.toDouble)
        }

        def insertOrCreateUser(name: String): Long = {
          val filter = users.filter { _.name === name }
          if (!filter.exists.run) {
            (users returning users.map(_.id)) +=(0L, name)
          } else {
            filter.first._1
          }
        }

        val userId = insertOrCreateUser(accountName)

        for (holdingToPersist <- holdingsToPersist) {
          persistHolding(userId, holdingToPersist)
        }
    }
  }
}