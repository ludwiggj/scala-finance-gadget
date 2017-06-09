package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.domain.FundChange.getFundChangesUpUntil
import models.org.ludwiggj.finance.persistence.database.TransactionsPerUserAndFund
import org.joda.time.LocalDate

case class HoldingSummaryList(holdings: List[HoldingSummary]) {
  def amountIn = holdings.map(_.amountIn).sum

  def total = holdings.map(_.total).sum

  def iterator = holdings.iterator

  override def toString = holdings.foldLeft("")(
    (str: String, holdingSummary: HoldingSummary) => str + holdingSummary + "\n"
  )
}

object HoldingSummaryList {

  def apply(transactions: TransactionsPerUserAndFund, userName: String, dateOfInterest: LocalDate): HoldingSummaryList = {

    def adjustHoldings(holdings: List[HoldingSummary], fundChange: FundChange): List[HoldingSummary] = {

      val holdingOutOption = holdings.find(_.price.fundName == fundChange.oldFundName)
      val holdingInOption = holdings.find(_.price.fundName == fundChange.newFundName)

      if (holdingOutOption.isDefined && holdingInOption.isDefined) {
        val holdingOut = holdingOutOption.get
        val holdingIn = holdingInOption.get

        val updatedHoldingIn = holdingIn.copy(amountIn = holdingIn.amountIn + holdingOut.amountIn)

        updatedHoldingIn :: holdings diff List(holdingOut, holdingIn)
      } else {
        holdings
      }
    }

    val holdings: List[HoldingSummary] =
      transactions.filterKeys(_._1 == userName).values.toList.map(
        {
          case (txs: Seq[Transaction], price: Price) => HoldingSummary(txs.toList, price)
        }
      )

    val holdingsWithFundChanges = getFundChangesUpUntil(dateOfInterest).foldLeft(holdings)(adjustHoldings)

    HoldingSummaryList(holdingsWithFundChanges.sorted)
  }
}