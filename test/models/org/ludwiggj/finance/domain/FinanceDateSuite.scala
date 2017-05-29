package models.org.ludwiggj.finance.domain

import java.sql.Date

import FinanceDate.sqlDateToFinanceDate
import org.scalatest.{FunSuite, Matchers}

class FinanceDateSuite extends FunSuite with Matchers {

  test("Finance date displays correct output") {
    FinanceDate("29/04/2014").toString should equal("29/04/2014")
  }

  test("Identical dates are equal") {
    FinanceDate("29/04/2014") should equal(FinanceDate("29/04/2014"))
  }

  test("Different dates are unequal") {
    FinanceDate("29/04/2014") should not equal (FinanceDate("30/04/2014"))
  }

  test("Date is not equal to null") {
    FinanceDate("29/04/2014") should not equal (null)
  }

  test("Can parse date with leading and trailing spaces") {
    FinanceDate("  15/03/2012 ").toString should equal("15/03/2012")
  }

  test("Can create finance date from sql date") {
    val sqlDate = Date.valueOf("2014-12-31")
    sqlDateToFinanceDate(sqlDate).toString should equal("31/12/2014")
  }

  test("Smaller than returns true if first date is smaller than second") {
    FinanceDate("29/04/2014") < FinanceDate("30/04/2014") should be(true)
  }

  test("Smaller than returns false if first date is larger than second") {
    FinanceDate("30/04/2014") < FinanceDate("29/04/2014") should be(false)
  }

  test("Compare returns 0 if first and second dates are equal") {
    FinanceDate("30/04/2014").compare(FinanceDate("30/04/2014")) should equal(0)
  }

  test("Parse date throws IllegalArgumentException if date has incorrect format") {
    intercept[IllegalArgumentException] {
      FinanceDate("15/03/12")
    }
  }
}