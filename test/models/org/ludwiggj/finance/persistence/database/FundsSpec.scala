package models.org.ludwiggj.finance.persistence.database

import FundsDatabase.fundNameToFundsRow
import Tables.FundsRow
import org.specs2.mutable.Specification

class FundsSpec extends Specification with DatabaseHelpers {

  // Following line required due to problem with EhCache
  // See https://groups.google.com/forum/#!topic/play-framework/6EqNOaUS0hE
  sequential

  "get" should {
    "return empty if fund is not present" in EmptySchema {
      FundsDatabase().get(solyentGreenFundName) must beEqualTo(None)
    }

    "return existing fund row if it is present" in SingleFund {
      FundsDatabase().get(capitalistsDreamFundName) must beSome(
        FundsRow(capitalistsDreamFundId, capitalistsDreamFundName)
      )
    }
  }

  "getOrInsert" should {
    "insert fund if it is not present" in EmptySchema {
      FundsDatabase().getOrInsert(solyentGreenFundName) must be_>(0L)
    }

    "return existing fund id if fund is present" in SingleFund {
      FundsDatabase().getOrInsert(capitalistsDreamFundName) must beEqualTo(capitalistsDreamFundId)
    }
  }
}