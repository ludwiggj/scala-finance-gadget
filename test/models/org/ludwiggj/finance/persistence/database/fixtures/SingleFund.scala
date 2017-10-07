package models.org.ludwiggj.finance.persistence.database.fixtures

import models.org.ludwiggj.finance.domain.FundName
import models.org.ludwiggj.finance.persistence.database.DatabaseLayer

trait SingleFund {

  this: DatabaseLayer =>

  val fundName = FundName("Capitalists Dream")

  val existingFundId = exec(Funds.insert(fundName))
}