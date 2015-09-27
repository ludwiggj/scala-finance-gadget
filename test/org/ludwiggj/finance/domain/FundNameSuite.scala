package org.ludwiggj.finance.domain

import org.ludwiggj.finance.TestFundNames
import org.scalatest.{FunSuite, Matchers}

class FundNameSuite extends FunSuite with Matchers {

  test("FundName and cleaned up version are equal") {
    new TestFundNames {
      fundNameBeforeNameCorrection should equal (fundNameAfterNameCorrection)
    }
  }
}