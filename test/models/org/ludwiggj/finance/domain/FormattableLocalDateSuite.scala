package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.aLocalDate
import org.joda.time.LocalDate
import org.scalatest.{FunSuite, Matchers}

class FormattableLocalDateSuite extends FunSuite with Matchers {

  test("String in correct format is converted to LocalDate") {
    val testDate = aLocalDate("15/03/2012")
    testDate should equal(LocalDate.parse("2012-03-15"))
  }

  test("Parse LocalDate with leading and trailing spaces") {
    val testDate = aLocalDate("  15/03/2012 ")
    testDate should equal(LocalDate.parse("2012-03-15"))
  }

  test("Parse LocalDate with date with leading zero") {
    val testDate = aLocalDate("01/03/2012")
    testDate should equal(LocalDate.parse("2012-03-01"))
  }

  test("Parse date throws IllegalArgumentException if year has two digits") {
    intercept[IllegalArgumentException] {
      aLocalDate("15/03/12")
    }
  }

  test("Parse date throws IllegalArgumentException if date has single digit") {
    intercept[IllegalArgumentException] {
      aLocalDate("1/03/2012")
    }
  }

  test("Parse date throws IllegalArgumentException if month has single digit") {
    intercept[IllegalArgumentException] {
      aLocalDate("01/3/2012")
    }
  }

  test("Finance date displays correct output") {
    FormattableLocalDate(aLocalDate("29/04/2014")).toString should equal("29/04/2014")
  }
}