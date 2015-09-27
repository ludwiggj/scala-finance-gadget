package org.ludwiggj.finance.domain

import java.sql.Date

import models.org.ludwiggj.finance.domain.FinanceDate
import org.scalatest.{FunSuite, Matchers}

class FinanceDateSuite extends FunSuite with Matchers {

  test("A finance date should display correct output") {
    FinanceDate("29/04/2014").toString should equal ("29/04/2014")
  }

  test("Equality returns true for equal dates") {
    FinanceDate("29/04/2014") should equal (FinanceDate("29/04/2014"))
  }

  test("Equality returns false for unequal dates") {
    FinanceDate("29/04/2014") should not equal (FinanceDate("30/04/2014"))
  }

  test("Equality returns false when other argument is null") {
    FinanceDate("29/04/2014") should not equal (null)
  }

  test("Can parse date with leading and trailing spaces") {
    FinanceDate("  15/03/2012 ").toString should equal ("15/03/2012")
  }

  test("Can create finance date from sql date") {
    val sqlDate = Date.valueOf("2014-12-31")
    FinanceDate(sqlDate).toString should equal ("31/12/2014")
  }

  test("Parse date throws IllegalArgumentException if date has incorrect format") {
    intercept[IllegalArgumentException] {
      FinanceDate("15/03/12")
    }
  }
}