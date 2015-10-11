package models.org.ludwiggj.finance.application.scratch

import java.sql.Date

import models.org.ludwiggj.finance.domain.{Price, Transaction}
import models.org.ludwiggj.finance.persistence.database.Tables.{Funds, Prices, Transactions, Users}
import models.org.ludwiggj.finance.persistence.database.TransactionsDatabase.InvestmentRegular
import play.api.Play
import play.api.Play.current
import play.api.db.DB
import play.api.test.FakeApplication
import scala.slick.driver.MySQLDriver.simple._

object ShowVariousTransactions extends App {

  def showTransactions() {
    val users: TableQuery[Users] = TableQuery[Users]
    val transactions: TableQuery[Transactions] = TableQuery[Transactions]
    val funds: TableQuery[Funds] = TableQuery[Funds]
    val prices: TableQuery[Prices] = TableQuery[Prices]

    lazy val db = Database.forDataSource(DB.getDataSource("finance"))

    db.withSession {
      implicit session =>

        def getTransactions(fundName: String, date: Date): List[Transaction] = {
          val userNameGraeme = "Graeme"

          def getTransactionTuples: List[TransactionTuple] = {
            (for {
              t <- transactions
              u <- t.usersFk
              if (u.name === userNameGraeme)
              p <- t.pricesFk if ((t.priceDate <= date) && (t.priceDate === p.date) && (t.fundId === p.fundId))
              f <- t.fundsFk if (f.name === fundName)
            } yield (f.name, t.date, t.description, t.amountIn, t.amountOut, t.priceDate, p.price, t.units)
              )
              .sortBy { case (fundName, transactionDate, _, _, _, _, _, _) => {
              (transactionDate, fundName)
            }
            }.list
          }

          getTransactionTuples map {
            transaction(userNameGraeme, _)
          }
        }

        def allFundNames() = {
          (for {
            fund <- funds.map(_.name)
          } yield (fund)).sorted.list
        }

        def lastFundPrice(fundName: String, date: Date): Price = {
          val (priceDate, price) = (for {
            p <- prices
            f <- p.fundsFk if ((f.name === fundName) && (p.date <= date))
          } yield (p.date, p.price)).sortBy { case (date, _) => date }.list.last
          Price(fundName, priceDate, price)
        }

        println("Holding, Cash in, Units added, Units subtracted, Units, Price Date, Price, Total, Increase, Increase %")

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

          val ins = (txs filter { tx => (tx.description == InvestmentRegular) || (tx.description == "Investment Lump Sum") } map {
            _.in.getOrElse(BigDecimal(0))
          }).sum

          val price = lastFundPrice(fundName, dateOfInterest)

          val totalUnits = unitsAdded - unitsSubtracted
          val total = totalUnits * price.inPounds
          val gain = total - ins

          println(f"$fundName%-50s £$ins%8.2f $unitsAdded%10.4f $unitsSubtracted%10.4f $totalUnits%10.4f "
            + f"${price.date} £${price.inPounds}%8.4f £$total%8.2f £$gain%8.2f ${gain / ins * 100}%8.2f%%")
        }
    }
  }

  private val application = FakeApplication()

  Play.start(application)

  showTransactions()

  Play.stop(application)
}