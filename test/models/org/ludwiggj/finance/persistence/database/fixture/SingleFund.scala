package models.org.ludwiggj.finance.persistence.database.fixture

import models.org.ludwiggj.finance.domain._
import models.org.ludwiggj.finance.domain.FundName.stringToFundName
import models.org.ludwiggj.finance.persistence.database._
import models.org.ludwiggj.finance.persistence.database.FundsDatabase.fundNameToFundsRow

trait SingleFund extends WithDbData {
  // Funds
  lazy val capitalistsDreamFundName: FundName = "Capitalists Dream"

  override def setupData() = {
    super.setupData()

    FundsDatabase().insert(capitalistsDreamFundName)
  }
}