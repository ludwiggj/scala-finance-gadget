package models.org.ludwiggj.finance.domain

import com.typesafe.config.{Config, ConfigFactory}
import scala.collection.JavaConversions._

case class FundChange(val oldFundName: FundName, val newFundName: FundName, val fromDate: FinanceDate) {

  def getFundIds: Option[(Long, Long)] = {
    for {
      oldFundId <- Fund.getId(oldFundName)
      newFundId <- Fund.getId(newFundName)
    } yield (oldFundId, newFundId)
  }
}

object FundChange {
  def apply(config: Config) = new FundChange(
    config.getString("oldFundName"),
    config.getString("newFundName"),
    config.getString("fromDate")
  )

  def getFundChangesUpUntil(dateOfInterest: FinanceDate): List[FundChange] = {
    def getFundChanges: List[FundChange] = {
        val config = ConfigFactory.load("fundChanges")
        (config.getConfigList("fundChanges") map (FundChange(_))).toList
    }

    getFundChanges.filter {
      _.fromDate <= dateOfInterest
    }
  }
}