package models.org.ludwiggj.finance.domain

import com.typesafe.config.ConfigFactory
import models.org.ludwiggj.finance.persistence.database.TransactionMap
import com.typesafe.config.Config
import scala.collection.JavaConversions._

case class HoldingSummaries(val holdings: List[HoldingSummary]) {
  def amountIn = holdings.map(_.amountIn).sum

  def total = holdings.map(_.total).sum

  def iterator = holdings.iterator

  override def toString = holdings.foldLeft("")(
    (str: String, holdingSummary: HoldingSummary) => str + holdingSummary + "\n"
  )
}

class FundChange(val fundNameOut: FundName, val fundNameIn: FundName, val fromDate: FinanceDate) {
}

object FundChange {
  def apply(config: Config) = new FundChange(
    config.getString("outFundName"),
    config.getString("inFundName"),
    config.getString("fromDate")
  )
}

object HoldingSummaries {

  def apply(transactions: TransactionMap, userName: String, dateOfInterest: FinanceDate, configFileName: String): HoldingSummaries = {

    val config = ConfigFactory.load(configFileName)

    val fundChanges = ((config.getConfigList("fundChanges") map (FundChange(_))).toList).filter {
      _.fromDate <= dateOfInterest
    }

    val holdings: List[HoldingSummary] =
      transactions.filterKeys(_._1 == userName).values.toList.map(
        {
          case (txs: Seq[Transaction], price: Price) => HoldingSummary(txs.toList, price)
        }
      )

    def adjustHoldings(holdings: List[HoldingSummary], fundChange: FundChange): List[HoldingSummary] = {

      val holdingOutOption = holdings.find(_.price.fundName == fundChange.fundNameOut)
      val holdingInOption = holdings.find(_.price.fundName == fundChange.fundNameIn)

      if (holdingOutOption.isDefined && holdingInOption.isDefined) {
        val holdingOut = holdingOutOption.get
        val holdingIn = holdingInOption.get

        val updatedHoldingIn = holdingIn.copy(amountIn = holdingIn.amountIn + holdingOut.amountIn)

        updatedHoldingIn :: holdings diff List(holdingOut, holdingIn)
      } else {
        holdings
      }
    }

    val holdingsWithFundChanges = fundChanges.foldLeft(holdings)(adjustHoldings)

    HoldingSummaries(holdingsWithFundChanges.sorted)
  }
}