package org.ludwiggj.finance.persistence

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import org.ludwiggj.finance.database.Tables.{Funds, Holdings, Users, Prices}
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

          def insertHolding(fundId: Long, holding: Holding) {
            try {
              holdings += (fundId, userId, holding.units.toDouble, holding.dateAsSqlDate)
            } catch {
              case ex: MySQLIntegrityConstraintViolationException =>
                println(s"Holding: ${ex.getMessage}")
            }
          }

          def insertPrice(fundId: Long, holding: Holding) {
            try {
              prices += (fundId, holding.dateAsSqlDate, holding.price.toDouble)
            } catch {
              case ex: MySQLIntegrityConstraintViolationException =>
                println(s"Price: ${ex.getMessage}")
            }
          }

          val fundId = insertOrCreateFund(holding.name)
          insertHolding(fundId, holding)
          insertPrice(fundId, holding)
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