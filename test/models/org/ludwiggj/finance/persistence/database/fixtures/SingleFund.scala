package models.org.ludwiggj.finance.persistence.database.fixtures

import models.org.ludwiggj.finance.domain.FundName
import models.org.ludwiggj.finance.persistence.database.DatabaseLayer
import models.org.ludwiggj.finance.persistence.database.PKs.PK

trait SingleFund {

  this: DatabaseLayer =>

  val fundName = FundName("Capitalists Dream")

  val existingFundId: PK[FundTable] = exec(Funds.insert(fundName))
}