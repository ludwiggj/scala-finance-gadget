package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.stringToLocalDate
import org.joda.time.LocalDate
import org.scalatest.{FunSuite, Matchers}

class FormattableLocalDateSuite extends FunSuite with Matchers {

  test("String in correct format is implicitly converted to LocalDate") {
    val testDate: LocalDate = "15/03/2012"
    testDate should equal(LocalDate.parse("2012-03-15"))
  }

  test("Parse LocalDate with leading and trailing spaces") {
    val testDate: LocalDate = "  15/03/2012 "
    testDate should equal(LocalDate.parse("2012-03-15"))
  }

  test("Parse LocalDate with date with leading zero") {
    val testDate: LocalDate = "01/03/2012"
    testDate should equal(LocalDate.parse("2012-03-01"))
  }

  test("Parse date throws IllegalArgumentException if year has two digits") {
    intercept[IllegalArgumentException] {
      val localDate: LocalDate = "15/03/12"
    }
  }

  test("Parse date throws IllegalArgumentException if date has single digit") {
    intercept[IllegalArgumentException] {
      val localDate: LocalDate = "1/03/2012"
    }
  }

  test("Parse date throws IllegalArgumentException if month has single digit") {
    intercept[IllegalArgumentException] {
      val localDate: LocalDate = "01/3/2012"
    }
  }

  test("Finance date displays correct output") {
    FormattableLocalDate("29/04/2014").toString should equal("29/04/2014")
  }
}