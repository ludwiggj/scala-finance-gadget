package org.ludwiggj.finance.application.scratch

import java.sql.Date

import org.ludwiggj.finance.domain.{FinanceDate, Transaction}
import org.ludwiggj.finance.persistence.database.Tables.{Funds, Prices, Transactions, Users}
import org.ludwiggj.finance.persistence.database.TransactionTuple

import scala.slick.driver.MySQLDriver.simple._

object ShowVariousTransactions extends App {

  def showTransactions() {
    val users: TableQuery[Users] = TableQuery[Users]
    val transactions: TableQuery[Transactions] = TableQuery[Transactions]
    val funds: TableQuery[Funds] = TableQuery[Funds]
    val prices: TableQuery[Prices] = TableQuery[Prices]

    val db = Database.forConfig("db")

    db.withSession {
      implicit session =>

        def getTransactions(fundName: String, date: Date): List[Transaction] = {

          def getTransactionTuples: List[TransactionTuple] = {
            (for {
              t <- transactions
              u <- t.usersFk if (u.name === "Graeme")
              p <- t.pricesFk if ((t.priceDate <= date) && (t.priceDate === p.priceDate) && (t.fundId === p.fundId))
              f <- t.fundsFk if (f.name === fundName)
            } yield (f.name, t.transactionDate, t.description, t.amountIn, t.amountOut, t.priceDate, p.price, t.units)
              )
              .sortBy { case (fundName, transactionDate, _, _, _, _, _, _) => {
              (transactionDate, fundName)
            }
            }.list
          }

          getTransactionTuples map {
            Transaction(_)
          }
        }

        def allFundNames() = {
          (for {
            fund <- funds.map(_.name)
          } yield (fund)).sorted.list
        }

        def lastFundPrice(fundName: String, date: Date) = {
          (for {
            p <- prices
            f <- p.fundsFk if ((f.name === fundName) && (p.priceDate <= date))
          } yield (p.price, p.priceDate)).sortBy { case (_, date) => date }.list.last
        }

        println("Holding, Cash in, Units added, Units subtracted, Units, Price, Price Date, Total")

        val dateOfInterest = Date.valueOf("2015-04-28")

        for (fundName <- allFundNames()) {

          val txs = getTransactions(fundName, dateOfInterest)

          val positiveUnits = (txs filter {
            _.in.getOrElse(BigDecimal(0)) > BigDecimal(0)
          })

          val negativeUnits = (txs filter {
            _.out.getOrElse(BigDecimal(0)) > BigDecimal(0)
          })

          val unitsAdded = positiveUnits.map {
            _.units
          }.sum

          val unitsSubtracted = negativeUnits.map {
            _.units
          }.sum

          val ins = (txs filter { tx => (tx.description == "Investment Regular") || (tx.description == "Investment Lump Sum") } map {
            _.in.getOrElse(BigDecimal(0))
          }).sum

          val priceInfo = lastFundPrice(fundName, dateOfInterest)

          //TODO should fundPriceDate have type FinanceDate?
          val (fundPrice, fundPriceDate) = priceInfo
          val totalUnits = unitsAdded - unitsSubtracted
          val total = totalUnits * fundPrice
          val gain = total - ins

          println(f"$fundName%-50s £$ins%8.2f $unitsAdded%10.4f $unitsSubtracted%10.4f $totalUnits%10.4f "
            + f"${FinanceDate(fundPriceDate)} £$fundPrice%8.4f £$total%8.2f £$gain%8.2f ${gain / ins * 100}%8.2f%%")
        }
    }
  }
  showTransactions()
}