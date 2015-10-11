package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.data.TestFundNames
import org.scalatest.{FunSuite, Matchers}

class FundNameSuite extends FunSuite with Matchers {

  test("FundName and cleaned up version are equal") {
    new TestFundNames {
      fundNameBeforeNameCorrection should equal (fundNameAfterNameCorrection)
    }
  }
}