package models.org.ludwiggj.finance.domain

import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.LocalDate
import scala.collection.JavaConverters._

case class FundChange(oldFundName: FundName, newFundName: FundName, fromDate: LocalDate) {
}

object FundChange {

  import models.org.ludwiggj.finance.aLocalDate

  def apply(config: Config) = new FundChange(
    FundName(config.getString("oldFundName")),
    FundName(config.getString("newFundName")),
    aLocalDate(config.getString("fromDate"))
  )

  def getFundChangesUpUntil(dateOfInterest: LocalDate): List[FundChange] = {
    def getFundChanges(): List[FundChange] = {
      val config = ConfigFactory.load("fundChanges")
      (config.getConfigList("fundChanges").asScala map (FundChange(_))).toList
    }

    import models.org.ludwiggj.finance.LocalDateOrdering._

    getFundChanges().filter {
      _.fromDate <= dateOfInterest
    }
  }
}