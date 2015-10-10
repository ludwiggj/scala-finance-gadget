package org.ludwiggj.finance.application

import models.org.ludwiggj.finance.domain.{Portfolio, FinanceDate, CashDelta}
import models.org.ludwiggj.finance.persistence.database.{Portfolios, TransactionsDatabase}
import play.api.Play
import play.api.test.FakeApplication

object ShowHoldingsFromTransactions extends App {

  def showPortfolios() {
    def showPortfolios(portfolios: List[Portfolio], dateOfInterest: FinanceDate) {

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

      // showPortfoliosForDate: START
      for (portfolio <- portfolios) {
        showPortfolio(portfolio)
      }

      val grandTotal = portfolios.foldRight(CashDelta(0, 0))(
        (portfolio: Portfolio, delta: CashDelta) => delta.add(portfolio.delta)
      )

      showTotal(s"GRAND TOTAL ($dateOfInterest)", grandTotal)
    }

    // showPortfolios: START
    for {
      dateOfInterest <- TransactionsDatabase().getRegularInvestmentDates()
    } {
      val portfolios = Portfolios().get(dateOfInterest)
      showPortfolios(portfolios, dateOfInterest)
    }
  }

  private val application = FakeApplication()

  Play.start(application)

  showPortfolios()

  Play.stop(application)
}