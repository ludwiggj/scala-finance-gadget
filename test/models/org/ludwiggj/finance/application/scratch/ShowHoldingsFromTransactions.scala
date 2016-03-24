package models.org.ludwiggj.finance.application.scratch

import java.sql.Date

import models.org.ludwiggj.finance.domain.{FinanceDate, Transaction}
import models.org.ludwiggj.finance.persistence.database.Tables.{Funds, Prices, Transactions, Users}
import Transaction.InvestmentRegular
import play.api.Play
import play.api.Play.current
import play.api.db.DB
import play.api.test.FakeApplication

import scala.slick.driver.MySQLDriver.simple._

object ShowHoldingsFromTransactions extends App {

  def showHoldings() {
    val users: TableQuery[Users] = TableQuery[Users]
    val transactions: TableQuery[Transactions] = TableQuery[Transactions]
    val funds: TableQuery[Funds] = TableQuery[Funds]
    val prices: TableQuery[Prices] = TableQuery[Prices]

    lazy val db = Database.forDataSource(DB.getDataSource("finance"))

    val zero = BigDecimal(0)
    type Holding = (String, String, Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], BigDecimal,
      Option[Date], BigDecimal, BigDecimal)

    db.withSession {
      implicit session =>

        def getHoldingsQueryForDate(dateOfInterest: Date) = {
          def netUnits(unitsIn: Column[Option[BigDecimal]], unitsOut: Column[Option[BigDecimal]]) = {
            unitsIn.getOrElse(BigDecimal(0)) - unitsOut.getOrElse(BigDecimal(0))
          }

          val dateDifference = SimpleFunction.binary[Date, Date, Int] ("DATEDIFF")

          val transactionsOfInterest = transactions filter {
            _.date <= dateOfInterest
          }

          val inAmounts = transactionsOfInterest
            .filter { tx => (tx.description === InvestmentRegular) || (tx.description === "Investment Lump Sum") }
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

          val latestPriceDates = prices
            .filter { p => (dateDifference(p.date, dateOfInterest)) <= 1 }
            .groupBy(p => p.fundId)
            .map { case (fundId, group) => {
            (fundId, group.map(_.date).max)

            // Should be able to use abs to get closest date either side of dateOfInterest, but hit various bugs...
            // See https://groups.google.com/forum/#!topic/scalaquery/lrumVNo3JE4

//            (fundId, group.map(prices => (dateDifference(prices.priceDate, dateOfInterest), prices.priceDate))
//              .sorted(_._1).take(1).map(_._2).max)
          }
          }

          val latestPrices =
            (for {
              (fundId, lastPriceDate) <- latestPriceDates
              p <- prices if p.fundId === fundId && p.date === lastPriceDate
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
            f <- funds if f.id === fundId
            u <- users if u.id === userId
          } yield (u.name, f.name, amount, addedUnits, subtractedUnits, totalUnits, lastPriceDate, lastPrice, value)
            ).sortBy { case (userName, fundName, _, _, _, _, _, _, _) => {
            (userName, fundName)
          }
          }
          finalTable
        }

        def showHoldingsForDate(dateOfInterest: FinanceDate, holdings: List[Holding]) {

          def gainOption(before: Option[BigDecimal], after: BigDecimal): (BigDecimal, BigDecimal) = {
            if (before.isDefined) gain(before.get, after) else (zero, zero)
          }

          def gain(before: BigDecimal, after: BigDecimal): (BigDecimal, BigDecimal) = {
            val gain = if (after != 0) after - before else zero
            val gainPct = if (gain != 0) gain / before * 100 else zero

            (gain, gainPct)
          }

          def showTotals(lineName: String, holdings: List[Holding]): Unit = {
            val totalBefore = holdings.map(_._3.getOrElse(zero)).sum
            val totalAfter = holdings.map(_._9).sum
            val totalGains = gain(totalBefore, totalAfter)
            val paddedLineName = lineName.padTo(49, " ").mkString("")

            println
            println(f"$paddedLineName £${totalBefore}%9.2f" +
              f"                                                             £${totalAfter}%9.2f" +
              f" £${totalGains._1}%9.2f  ${totalGains._2}%6.2f %%")
            println
          }

          def showHoldingsForUserAndDate(userName: String, usersHoldings: List[Holding]): Unit = {
            def showHeader() = {
              println(s"$userName, Date ${dateOfInterest}")
              println
              println("Fund Name                                          Amount In    Units In  Units Out  Total Units"
                + "  Price Date      Price       Total       Gain    Gain %")
              println("---------                                          ---------    --------  ---------  -----------"
                + "  ----------      -----       -----       ----    ------")
            }

            def showBody() = {
              usersHoldings map {
                holding =>
                  val (_, fundName, amountIn, addedUnits, subtractedUnits, totalUnits, lastPriceDate, lastPrice, value) = holding
                  val gains = gainOption(amountIn, value)

                  println(f"${fundName}%-50s £${amountIn.getOrElse(zero)}%8.2f  ${addedUnits.getOrElse(zero)}%10.4f" +
                    f" ${subtractedUnits.getOrElse(zero)}%10.4f   ${totalUnits}%10.4f  ${lastPriceDate.get}" +
                    f"  £${lastPrice}%8.4f  £${value}%9.2f  £${gains._1}%8.2f  ${gains._2}%6.2f %%")
              }
            }

            // Start here (showHoldingsForUserAndDate)
            showHeader()
            showBody()
            showTotals(s"Totals ($userName, ${dateOfInterest})", usersHoldings)
          }

          // Start here (showHoldingsForDate)
          val userNames = holdings.map {
            _._1
          }.distinct

          for {
            userName <- userNames
          } {
            val usersHoldings = holdings filter {
              _._1 == userName
            }
            showHoldingsForUserAndDate(userName, usersHoldings)
          }
          showTotals(s"GRAND TOTAL (${dateOfInterest})", holdings)

        }

        // Start here (showHoldings)
//        val today = new Date(115, 4, 31)
//        showHoldingsForDate(today, getHoldingsQueryForDate(today).list)

        val regularInvestmentDates = transactions
          .filter { tx => tx.description === InvestmentRegular }
          .map {
          _.date
        }.sorted.list.reverse.distinct

        for {
          dateOfInterest <- regularInvestmentDates
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