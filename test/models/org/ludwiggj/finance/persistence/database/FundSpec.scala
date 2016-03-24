package models.org.ludwiggj.finance.persistence.database

import Tables.FundsRow
import models.org.ludwiggj.finance.domain.Fund
import models.org.ludwiggj.finance.domain.Fund.fundNameToFundsRow
import org.scalatest.{BeforeAndAfter, DoNotDiscover}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}

@DoNotDiscover
class FundSpec extends PlaySpec with DatabaseHelpers with ConfiguredApp with BeforeAndAfter {

  before {
    DatabaseCleaner.recreateDb()
  }

  "get" must {
    "return empty if fund is not present" in {
      EmptySchema.loadData()

      Fund.get(solyentGreenFundName) must equal(None)
    }

    "return existing fund row if it is present" in {
      SingleFund.loadData()

      Fund.get(capitalistsDreamFundName) mustBe Some(
        FundsRow(capitalistsDreamFundId, capitalistsDreamFundName)
      )
    }
  }

  "getOrInsert" should {
    "insert fund if it is not present" in {
      EmptySchema.loadData()

      Fund.getOrInsert(solyentGreenFundName) must be > 0L
    }

    "return existing fund id if fund is present" in {
      SingleFund.loadData()

      Fund.getOrInsert(capitalistsDreamFundName) must equal(capitalistsDreamFundId)
    }
  }
}