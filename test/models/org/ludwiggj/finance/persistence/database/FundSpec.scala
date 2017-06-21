package models.org.ludwiggj.finance.persistence.database

import Tables.{FundRow, FundTable}
import models.org.ludwiggj.finance.domain.{Fund, FundName}
import models.org.ludwiggj.finance.persistence.database.PKs.PK
import org.scalatest.{BeforeAndAfter, DoNotDiscover}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}

@DoNotDiscover
class FundSpec extends PlaySpec with DatabaseHelpers with ConfiguredApp with BeforeAndAfter {

  before {
    TestDatabase.recreateSchema()
  }

  private val nonExistentFund = FundName("fundThatIsNotPresent")

  "get" must {
    "return empty if fund is not present" in {
      EmptySchema.loadData()

      Fund.get(nonExistentFund) must equal(None)
    }

    "return existing fund row if it is present" in {
      SingleFund.loadData()

      Fund.get(SingleFund.fundName) mustBe Some(
        FundRow(SingleFund.fundId, SingleFund.fundName)
      )
    }
  }

  "getOrInsert" should {
    "insert fund if it is not present" in {
      EmptySchema.loadData()

      Fund.getOrInsert(nonExistentFund) must be > PK[FundTable](0L)
    }

    "return existing fund id if fund is present" in {
      SingleFund.loadData()

      Fund.getOrInsert(SingleFund.fundName) must equal(SingleFund.fundId)
    }
  }
}