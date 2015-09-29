package org.ludwiggj.finance.application

import java.sql.Date
import models.org.ludwiggj.finance.domain._
import play.api.Play
import play.api.Play.current
import play.api.db.DB
import play.api.test.FakeApplication
import models.org.ludwiggj.finance.persistence.database.Tables.{Users, Transactions, Funds, Prices}
import scala.slick.driver.MySQLDriver.simple._

object ReworkedShowHoldingsFromTransactions extends App {

  def showHoldings() {
    lazy val db = Database.forDataSource(DB.getDataSource("finance"))

    val zero = BigDecimal(0)
    type Holding = (String, String, Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], BigDecimal,
      Option[Date], BigDecimal, BigDecimal)

    db.withSession {
      implicit session =>

        def getRegularInvestmentDates: List[Date] = {
          Transactions
            .filter { tx => tx.description === "Investment Regular" }
            .map {
             _.date
          }.sorted.list.reverse.distinct
        }

        def getHoldingsQueryForDate(dateOfInterest: Date) = {
          def netUnits(unitsIn: Column[Option[BigDecimal]], unitsOut: Column[Option[BigDecimal]]) = {
            unitsIn.getOrElse(BigDecimal(0)) - unitsOut.getOrElse(BigDecimal(0))
          }

          val dateDifference = SimpleFunction.binary[Date, Date, Int]("DATEDIFF")

          val transactionsOfInterest = Transactions filter {
            _.date <= dateOfInterest
          }

          val inAmounts = transactionsOfInterest
            .filter { tx => (tx.description === "Investment Regular") || (tx.description === "Investment Lump Sum") }
            .groupBy(t => (t.fundId, t.userId))
            .map { case ((fundId, userId), group) => {
            (fundId, userId, group.map(_.amountIn).sum)
          }
          }

          val unitsAdded = transactionsOfInterest
            .filter { tx => tx.amountIn > BigDecimal(0) }
            .groupBy(t => (t.fundId, t.userId))
            .map { case ((fundId, userId), group) => {
            (fundId, userId, group.map(_.units).sum)
          }
          }

          val unitsSubtracted = transactionsOfInterest
            .filter { tx => tx.amountOut > BigDecimal(0) }
            .groupBy(t => (t.fundId, t.userId))
            .map { case ((fundId, userId), group) => {
            (fundId, userId, group.map(_.units).sum)
          }
          }

          val latestPriceDates = Prices
            .filter { p => (dateDifference(p.date, dateOfInterest)) <= 1 }
            .groupBy(p => p.fundId)
            .map { case (fundId, group) => {
            (fundId, group.map(_.date).max)
          }
          }

          val latestPrices =
            (for {
              (fundId, lastPriceDate) <- latestPriceDates
              p <- Prices if p.fundId === fundId && p.date === lastPriceDate
            } yield (p.fundId, lastPriceDate, p.price))

          val joinedTable = ((unitsAdded leftJoin unitsSubtracted on ((added, subtracted) => {

            val (uaFundId, uaUserId, _) = added
            val (usFundId, usUserId, _) = subtracted

            uaFundId === usFundId && uaUserId === usUserId

          })) leftJoin inAmounts on ((units, amounts) => {

            val ((uaFundId, uaUserId, _), _) = units
            val (aFundId, aUserId, _) = amounts

            uaFundId === aFundId && uaUserId === aUserId

          }) leftJoin latestPrices on ((unitsAmounts, latestPrices) => {

            val (((uaFundId, _, _), _), _) = unitsAmounts
            val (lpFundId, _, _) = latestPrices

            uaFundId === lpFundId

          }))
            .map { case ((((fundId, userId, addedUnits), (_, _, subtractedUnits)), (_, _, amount)), (_, lastPriceDate, lastPrice)) =>

            val totalUnits = netUnits(addedUnits, subtractedUnits)

            (fundId, userId, amount, addedUnits, subtractedUnits, totalUnits, lastPriceDate, lastPrice, totalUnits * lastPrice)
          }

          val finalTable = (for {
            (fundId, userId, amount, addedUnits, subtractedUnits, totalUnits, lastPriceDate, lastPrice, value) <- joinedTable
            f <- Funds if f.id === fundId
            u <- Users if u.id === userId
          } yield (u.name, f.name, amount, addedUnits, subtractedUnits, totalUnits, lastPriceDate, lastPrice, value)
            ).sortBy { case (userName, fundName, _, _, _, _, _, _, _) => {
            (userName, fundName)
          }
          }
          finalTable
        }

        def showHoldingsForDate(dateOfInterest: FinanceDate, holdings: List[Holding]) {

          def getPortfolios: List[Portfolio] = {
            val userNames = holdings.map {
              _._1
            }.distinct

            val portfolios = userNames map { userName =>
              val usersHoldings: List[HoldingSummary] = holdings filter {
                _._1 == userName
              } map {
                h => HoldingSummary(h._3.get, h._4.get, h._5, Price(h._2, h._7.get, h._8))
              }
              Portfolio(userName, dateOfInterest, usersHoldings)
            }
            portfolios
          }

          def showPortfolio(portfolio: Portfolio): Unit = {
            def showPortfolioHeader() = {
              println(s"${portfolio.userName}, Date $dateOfInterest")
              println
              println("Fund Name                                          Amount In    Units In  Units Out  Total Units"
                + "  Price Date      Price       Total       Gain    Gain %")
              println("---------                                          ---------    --------  ---------  -----------"
                + "  ----------      -----       -----       ----    ------")
            }

            // showPortfolio: START
            showPortfolioHeader()
            println(portfolio)
            showTotal(s"Totals (${portfolio.userName}, $dateOfInterest)", portfolio.delta)
          }

          def showTotal(lineName: String, delta: CashDelta): Unit = {
            val paddedLineName = lineName.padTo(49, " ").mkString("")

            println
            println(f"$paddedLineName $delta")
            println
          }

          // showHoldingsForDate: START
          val portfolios: List[Portfolio] = getPortfolios

          for (portfolio <- portfolios) {
            showPortfolio(portfolio)
          }

          val grandTotal = portfolios.foldRight(CashDelta(0, 0))(
            (portfolio: Portfolio, delta: CashDelta) => delta.add(portfolio.delta)
          )

          showTotal(s"GRAND TOTAL ($dateOfInterest)", grandTotal)
        }

        // showHoldings: START
        for {
          dateOfInterest <- getRegularInvestmentDates
        } {
          showHoldingsForDate(dateOfInterest, getHoldingsQueryForDate(dateOfInterest).list)
        }
    }
  }

  private val application = FakeApplication()

  Play.start(application)

  showHoldings()

  Play.stop(application)
}