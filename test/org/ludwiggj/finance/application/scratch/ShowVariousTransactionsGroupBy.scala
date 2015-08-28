package org.ludwiggj.finance.application.scratch

import java.sql.Date
import models.org.ludwiggj.finance.domain.Transaction
import models.org.ludwiggj.finance.persistence.database.Tables.{Funds, Prices, Transactions, Users}
import models.org.ludwiggj.finance.persistence.database.TransactionTuple
import play.api.Play
import play.api.Play.current
import play.api.db.DB
import play.api.test.FakeApplication
import scala.slick.driver.MySQLDriver.simple._

object ShowVariousTransactionsGroupBy extends App {

  def showTransactions() {
    val users: TableQuery[Users] = TableQuery[Users]
    val transactions: TableQuery[Transactions] = TableQuery[Transactions]
    val funds: TableQuery[Funds] = TableQuery[Funds]
    val prices: TableQuery[Prices] = TableQuery[Prices]

    lazy val db = Database.forDataSource(DB.getDataSource("finance"))

    db.withSession {
      implicit session =>

        def getFundName(fundId: Long) = {
          funds.filter(f => f.id === fundId).map(_.name).run
        }

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
          println(s"$fundName $date")
          (for {
            p <- prices
            f <- p.fundsFk if ((f.name === fundName) && (p.priceDate <= date))
          } yield (p.price, p.priceDate)).sortBy { case (_, date) => date }
          //.list.last
        }

        println("Holding, Cash in, Units added, Units subtracted, Units, Price, Price Date, Total")

        val dateOfInterest = Date.valueOf("2014-12-30")

        println("In amounts......")
        println("================")
        val ins = transactions
          .filter { tx => (tx.description === "Investment Regular") || (tx.description === "Investment Lump Sum") }
          .groupBy(t => (t.fundId, t.userId))
          .map { case ((fundId, userId), group) => {
          (fundId, userId, group.map(_.amountIn).sum)
        }
        }
          .list
          .map { case (fundId, userId, x) => (getFundName(fundId), userId, x) }
        //          .map(println)

        println("Units added......")
        println("=================")
        val unitsAdded = transactions
          .filter { tx => tx.amountIn > BigDecimal(0) }
          .groupBy(t => (t.fundId, t.userId))
          .map { case ((fundId, userId), group) => {
          (fundId, userId, group.map(_.units).sum)
        }
        }
        //          .list
        //          .map { case (fundId, userId, x) => (getFundName(fundId), userId, x) }
        //          .map(println)

        println("Units subtracted......")
        println("======================")
        val unitsSubtracted = transactions
          .filter { tx => tx.amountOut > BigDecimal(0) }
          .groupBy(t => (t.fundId, t.userId))
          .map { case ((fundId, userId), group) => {
          (fundId, userId, group.map(_.units).sum)
        }
        }
        //          .list
        //          .map { case (fundId, userId, x) => (getFundName(fundId), userId, x) }
        //          .map(println)

        //        (for {
        //          (fundName1, userId1, unitsAdded1) <- unitsAdded
        //          (fundName2, userId2, unitsSubtracted2) <- unitsSubtracted if (fundName1 == fundName2) && (userId1 == userId2)
        //        } yield (fundName1, userId1, unitsAdded1, unitsSubtracted2, (unitsAdded1.get - unitsSubtracted2.get))).map(println)


        (unitsAdded leftJoin unitsSubtracted on ((added, subtracted) => {
          added._1 === subtracted._1 && added._2 === subtracted._2
        }))
          .map { case ((fundId, userId, addedUnits), (_, _, subtractedUnits)) => (fundId, userId, addedUnits, subtractedUnits) }.run
        //          .map(println)


        val joinedTransactions = transactions
          .flatMap(tx => funds.filter(fn => tx.fundId === fn.id).map(f => (f.name, tx.userId, tx.amountIn, tx.amountOut, tx.units)))
          .flatMap { case (fundName, userId, amountIn, amountOut, units) =>
          users.filter(u => userId === u.id).map(u => (fundName, u.name, amountIn, amountOut, units))
        }


        //        println("Units added......")
        //        println("=================")
        //        val unitsAdded2 = transactions
        //          .filter { tx => tx.amountIn > BigDecimal(0) }
        //          .flatMap(tx => funds.filter(fn => tx.fundId === fn.id).map(f => (f.name, tx.userId, tx.units)))
        //          .groupBy { case (fundName, userId, _) => (fundName, userId) }
        //          .map { case ((fundId, userId), group) => {
        //          (fundId, userId, group.map(_._3).sum)
        //        }
        //        }.list.map(println)

        println("Units added......")
        println("=================")
        val unitsAdded2 = joinedTransactions
          .filter { case (_, _, amountIn, _, _) => amountIn > BigDecimal(0) }
          .groupBy { case (fundName, userName, _, _, _) => (fundName, userName) }
          .map { case ((fundName, userName), group) => {
          (fundName, userName, group.map(_._5).sum)
        }
        }
        //          .list.map(println)

        println("Units subtracted......")
        println("======================")
        val unitsSubtracted2 = joinedTransactions
          .filter { case (_, _, _, amountOut, _) => amountOut > BigDecimal(0) }
          .groupBy { case (fundName, userName, _, _, _) => (fundName, userName) }
          .map { case ((fundName, userName), group) => {
          (fundName, userName, group.map(_._5).sum)
        }
        }
        //          .list.map(println)

        def fundPrices(fundName: Column[String]) = {
          (for {
            p <- prices
            f <- p.fundsFk if ((f.name === fundName))
          } yield (p.price, p.priceDate)).sortBy { case (_, date) => date }
        }

        val fundPricesCompiled = Compiled(fundPrices _)

        val table = (unitsAdded2 leftJoin unitsSubtracted2 on ((added, subtracted) => {
          added._1 === subtracted._1 && added._2 === subtracted._2
        }))
          .map { case ((fundName, userName, addedUnits), (_, _, subtractedUnits)) =>
          (fundName, userName, addedUnits, subtractedUnits, (addedUnits.getOrElse(0) - subtractedUnits.getOrElse(0)))
          //          fundPricesCompiled(fundName).list.last)
        }.run

        table.map(println)

        println(fundPricesCompiled("F&C Responsible UK Equity Growth 1 Fund Inc").list.last)

        println("Last prices ......")
        println("================")
        val latestPriceDates = prices
          .filter { p => p.priceDate <= dateOfInterest }
          .groupBy(p => p.fundId)
          .map { case (fundId, group) => {
          (fundId, group.map(_.priceDate).max)
        }
        }

        val latestPrices =
          (for {
            (fundId, lastPriceDate) <- latestPriceDates
            p <- prices if p.fundId === fundId && p.priceDate === lastPriceDate
          } yield (p.fundId, lastPriceDate, p.price)).list.map(println)
    }
  }

  private val application = FakeApplication()

  Play.start(application)

  showTransactions()

  Play.stop(application)
}

/*
val joinedTable = ((unitsAdded leftJoin unitsSubtracted on ((added, subtracted) => {
          val (uaFundId, uaUserId, _) = added
          val (usFundId, usUserId, _) = subtracted

          uaFundId === usFundId && uaUserId === usUserId
        })) leftJoin inAmounts on ((units, amounts) => {
          val ((uaFundId, uaUserId, _), _) = units
          val (aFundId, aUserId, _) = amounts

          uaFundId === aFundId && uaUserId === aUserId

        })).map { case (((fundId, userId, addedUnits), (_, _, subtractedUnits)), (_, _, amount)) =>
          (fundId, userId, amount, addedUnits, subtractedUnits, netUnits(addedUnits, subtractedUnits))
        }

        joinedTable.list.map(println)
 */


/*
        println
        println("Joined Units..........")
        println("======================")
        println

        val joinedUnits = (unitsAdded leftJoin unitsSubtracted on ((added, subtracted) => {
          added._1 === subtracted._1 && added._2 === subtracted._2
        }))
          .map { case ((fundId, userId, addedUnits), (_, _, subtractedUnits)) => (fundId, userId, addedUnits, subtractedUnits) }

        joinedUnits.list.map(println)

        val joinedTransactions = transactions
                  .flatMap(tx => funds.filter(fn => tx.fundId === fn.id).map(f => (f.name, tx.userId, tx.amountIn, tx.amountOut, tx.units)))
                  .flatMap { case (fundName, userId, amountIn, amountOut, units) =>
                  users.filter(u => userId === u.id).map(u => (fundName, u.name, amountIn, amountOut, units))
        }

        val joinedUnits3 = (unitsAdded leftJoin unitsSubtracted on ((added, subtracted) => {
          val (uaFundId, uaUserId, _) = added
          val (usFundId, usUserId, _) = subtracted
          uaFundId === usFundId && uaUserId === usUserId
        }))
          .map { case ((fundId, userId, addedUnits), (_, _, subtractedUnits)) => (fundId, userId, addedUnits, subtractedUnits) }
              joinedUnits3.list.map(println)

        //        val table = for {
        //          ((a, ua)) <- inAmounts leftJoin unitsAdded on (_._1 === _._1)
        //          if a._2 === ua._2.?
        //        } yield (a._1, a._2, a._3, ua._3)

        //        val table = for {
        //          ((a, ua), us) <- inAmounts leftJoin unitsAdded on (_._1.? === _._1.?) leftJoin unitsSubtracted on (_._1._1.? === _._1.?)
        //          if a._2.? === ua._2.? && a._2.? === us._2.?
        //        } yield (a._1.?, a._2.?, a._3.?)


        //          (inAmounts leftJoin unitsAdded leftJoin unitsSubtracted on ((amounts, added, subtracted) => {
        //          amounts._1 === subtracted._1 && added._2 === subtracted._2 && added._1 === subtracted._1 && added._2 === subtracted._2
        //        }))
        //          .map { case ((fundName, userName, addedUnits), (_, _, subtractedUnits)) =>
        //          (fundName, userName, addedUnits, subtractedUnits, (addedUnits.getOrElse(0) - subtractedUnits.getOrElse(0)))
        //        }

        //        table.list.map(println)

        println
        println("Units added 2......")
        println("=================")
        println
        val unitsAdded2 = joinedTransactions
          .filter { case (_, _, amountIn, _, _) => amountIn > BigDecimal(0) }
          .groupBy { case (fundName, userName, _, _, _) => (fundName, userName) }
          .map { case ((fundName, userName), group) => {
          (fundName, userName, group.map(_._5).sum)
        }
        }

        unitsAdded2.list.map(println)

        println
        println("Units subtracted 2....")
        println("======================")
        println
        val unitsSubtracted2 = joinedTransactions
          .filter { case (_, _, _, amountOut, _) => amountOut > BigDecimal(0) }
          .groupBy { case (fundName, userName, _, _, _) => (fundName, userName) }
          .map { case ((fundName, userName), group) => {
          (fundName, userName, group.map(_._5).sum)
        }
        }

        unitsSubtracted2.list.map(println)

        println
        println("Table........")
        println("=============")
        println
        val table = (unitsAdded2 leftJoin unitsSubtracted2 on ((added, subtracted) => {
          added._1 === subtracted._1 && added._2 === subtracted._2
        }))
          .map { case ((fundName, userName, addedUnits), (_, _, subtractedUnits)) =>
          (fundName, userName, addedUnits, subtractedUnits, (addedUnits.getOrElse(0) - subtractedUnits.getOrElse(0)))
        }

        table.list.map(println)
 */