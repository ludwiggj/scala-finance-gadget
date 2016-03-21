package models.org.ludwiggj.finance.persistence.database

import FundsDatabase.fundNameToFundsRow
import Tables.FundsRow
import org.scalatest.{BeforeAndAfter, DoNotDiscover}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}

@DoNotDiscover
class FundsSpec extends PlaySpec with DatabaseHelpers with ConfiguredApp with BeforeAndAfter {

  before {
    DatabaseCleaner.recreateDb()
  }

  "get" must {
    "return empty if fund is not present" in {
      EmptySchema.loadData()

      FundsDatabase().get(solyentGreenFundName) must equal(None)
    }

    "return existing fund row if it is present" in {
      SingleFund.loadData()

      FundsDatabase().get(capitalistsDreamFundName) mustBe Some(
        FundsRow(capitalistsDreamFundId, capitalistsDreamFundName)
      )
    }
  }

  "getOrInsert" should {
    "insert fund if it is not present" in {
      EmptySchema.loadData()

      FundsDatabase().getOrInsert(solyentGreenFundName) must be > 0L
    }

    "return existing fund id if fund is present" in {
      SingleFund.loadData()

      FundsDatabase().getOrInsert(capitalistsDreamFundName) must equal(capitalistsDreamFundId)
    }
  }
}