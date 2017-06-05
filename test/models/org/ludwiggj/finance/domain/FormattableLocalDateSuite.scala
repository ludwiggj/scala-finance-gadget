package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.stringToLocalDate
import org.joda.time.LocalDate
import org.scalatest.{FunSuite, Matchers}

class FormattableLocalDateSuite extends FunSuite with Matchers {

  test("Finance date displays correct output") {
    FormattableLocalDate("29/04/2014").toString should equal("29/04/2014")
  }

  test("Can parse date with leading and trailing spaces") {
    FormattableLocalDate("  15/03/2012 ").toString should equal("15/03/2012")
  }

  test("Parse date throws IllegalArgumentException if date has incorrect format") {
    intercept[IllegalArgumentException] {
      val localDate: LocalDate = "15/03/12"
    }
  }
}