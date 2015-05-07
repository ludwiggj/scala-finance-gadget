package org.ludwiggj.finance.application

import java.sql.Date

import org.ludwiggj.finance.domain.Transaction
import org.ludwiggj.finance.persistence.database.Tables.{Funds, Prices, Transactions, Users}
import org.ludwiggj.finance.persistence.database.TransactionTuple

import scala.slick.driver.MySQLDriver.simple._

object TransactionsQueries extends App {

  def showTransactions() {
    val users: TableQuery[Users] = TableQuery[Users]
    val transactions: TableQuery[Transactions] = TableQuery[Transactions]
    val funds: TableQuery[Funds] = TableQuery[Funds]
    val prices: TableQuery[Prices] = TableQuery[Prices]

    val db = Database.forConfig("db")

    db.withSession {
      implicit session =>

        def displayTransactions(txs: List[Transaction]) = {
          println(s"Total no of txs = ${txs.size}")
          txs foreach {
            tx =>
              println(f"${tx.date}  ${tx.holdingName}%-50s ${tx.description}%-25s £${tx.in.get}%7.2f  " +
                f"£${tx.out.get}%7.2f  ${tx.priceDate}  £${tx.priceInPounds}%8.4f ${tx.units}%10.4f")
          }
        }

        def getTransactions(): List[Transaction] = {

          def getTransactionTuples: List[TransactionTuple] = {
            (for {
              t <- transactions
              u <- t.usersFk if (u.name === "Graeme")
              p <- t.pricesFk if ((t.priceDate === p.priceDate) && (t.fundId === p.fundId))
              f <- t.fundsFk
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

        def getTransactionsCashInvestments() = {
          (for {
            t <- transactions if ((t.description === "Investment Regular") ||
            (t.description === "Investment Lump Sum"))
            u <- t.usersFk if (u.name === "Graeme")
            p <- t.pricesFk if ((t.priceDate === p.priceDate) && (t.fundId === p.fundId))
            f <- t.fundsFk
          } yield (f.name, t.transactionDate, t.description, t.amountIn, t.amountOut, t.priceDate, p.price, t.units)
            ).sortBy(row => {
            val (fundName, transactionDate, _, _, _, _, _, _) = row
            (transactionDate, fundName)
          }).list map {
            Transaction(_)
          }
        }

        def getTransactionsDividends() = {
          (for {
            t <- transactions if ((t.description === "Dividend Reinvestment"))
            u <- t.usersFk if (u.name === "Graeme")
            p <- t.pricesFk if ((t.priceDate === p.priceDate) && (t.fundId === p.fundId))
            f <- t.fundsFk
          } yield (f.name, t.transactionDate, t.description, t.amountIn, t.amountOut, t.priceDate, p.price, t.units)
            ).sortBy(row => {
            val (fundName, transactionDate, _, _, _, _, _, _) = row
            (transactionDate, fundName)
          }).list map {
            Transaction(_)
          }
        }


        println("Holding, Cash in, Units added, Units subtracted, Units, Price, Price Date, Total")

        val dateOfInterest = Date.valueOf("2015-04-28")

        var txs = getTransactions()

        println(s"All txs, total (${txs.size})")
        displayTransactions(txs)

        println("============")
        println("Positive txs")
        println("============")

        val positiveUnits = (txs filter {
          _.in.getOrElse(BigDecimal(0)) > BigDecimal(0)
        })

        displayTransactions(positiveUnits)

        println("============")
        println("Negative txs")
        println("============")
        val negativeUnits = (txs filter {
          _.out.getOrElse(BigDecimal(0)) > BigDecimal(0)
        })

        displayTransactions(negativeUnits)

        val unitsAdded = positiveUnits.map {
          _.units
        }.sum
        val unitsSubtracted = negativeUnits.map {
          _.units
        }.sum

        txs = getTransactionsCashInvestments()
        println(s"Investment Regular txs")
        displayTransactions(txs)

        val ins = (txs filter { tx => (tx.description == "Investment Regular") || (tx.description == "Investment Lump Sum") } map {
          _.in.getOrElse(BigDecimal(0))
        }).sum

        txs = getTransactionsDividends()
        println(s"Dividends txs")
        displayTransactions(txs)
    }
  }

  showTransactions()
}