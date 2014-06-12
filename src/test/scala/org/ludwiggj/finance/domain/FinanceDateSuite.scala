package org.ludwiggj.finance.domain

import org.scalatest.{Matchers, FunSuite}

class FinanceDateSuite extends FunSuite with Matchers {

  test("A finance date should display correct output") {
    assert(FinanceDate("29/04/2014").toString == "29/04/2014")
  }

  test("Equality returns true for equal dates") {
    assert(FinanceDate("29/04/2014").equals(FinanceDate("29/04/2014")) == true)
  }

  test("Equality returns false for unequal dates") {
    assert(FinanceDate("29/04/2014").equals(FinanceDate("30/04/2014")) == false)
  }

  test("Equality returns false when other argument is null") {
    assert(FinanceDate("29/04/2014").equals(null) == false)
  }

  test("Can parse date with leading and trailing spaces") {
    assert(FinanceDate("  15/03/2012 ").toString == "15/03/2012")
  }

  test("Parse date throws IllegalArgumentException if date has incorrect format") {
    intercept[IllegalArgumentException] {
      FinanceDate("15/03/12")
    }
  }
}