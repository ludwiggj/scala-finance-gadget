package models.org.ludwiggj.finance.domain

import com.typesafe.config.{Config, ConfigFactory}
import models.org.ludwiggj.finance.persistence.database.PKs.PK
import models.org.ludwiggj.finance.persistence.database.Tables.FundTable
import org.joda.time.LocalDate

import scala.collection.JavaConversions._

case class FundChange(oldFundName: FundName, newFundName: FundName, fromDate: LocalDate) {

  def getFundIds: Option[(PK[FundTable], PK[FundTable])] = {
    for {
      oldFundId <- Fund.getId(oldFundName)
      newFundId <- Fund.getId(newFundName)
    } yield (oldFundId, newFundId)
  }
}

object FundChange {

  import models.org.ludwiggj.finance.stringToLocalDate

  def apply(config: Config) = new FundChange(
    config.getString("oldFundName"),
    config.getString("newFundName"),
    config.getString("fromDate")
  )

  def getFundChangesUpUntil(dateOfInterest: LocalDate): List[FundChange] = {
    def getFundChanges: List[FundChange] = {
      val config = ConfigFactory.load("fundChanges")
      (config.getConfigList("fundChanges") map (FundChange(_))).toList
    }

    import models.org.ludwiggj.finance.LocalDateOrdering._

    getFundChanges.filter {
      _.fromDate <= dateOfInterest
    }
  }
}