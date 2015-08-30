package org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.persistence.database.FundsDatabase
import models.org.ludwiggj.finance.persistence.database.FundsDatabase.fundsRowWrapper
import org.specs2.mutable.Specification

class FundsSpec extends Specification with DatabaseHelpers {
  "Fund" should {
    "be able to be inserted" in EmptySchema {
      val fund = "Solyent Green"
      val fundId = FundsDatabase().getOrInsert(fund)
      fundId must be_>(0L)
    }

    "be able to be found" in SingleFund {
      val fundId = FundsDatabase().getOrInsert("Capitalists Dream")
      fundId must beEqualTo(capitalistsDreamFundId)
    }
  }
}