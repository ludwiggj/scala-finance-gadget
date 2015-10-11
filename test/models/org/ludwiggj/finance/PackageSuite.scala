package models.org.ludwiggj.finance

import java.sql.Date
import org.scalatest.{FunSuite, Matchers}

class PackageSuite extends FunSuite with Matchers {

  test("String in correct format is implicitly converted to sql date") {
    val testDate: Date = "21/06/1992"
    testDate should equal(Date.valueOf("1992-06-21"))
  }

  test("String with single digit date and month is implicitly converted to sql date") {
    val testDate: Date = "2/6/1992"
    testDate should equal(Date.valueOf("1992-6-2"))
  }

  test("String in incorrect format is not implicitly converted to sql date") {
    intercept[IllegalArgumentException] {
      val testDate: Date = "2/6/92"
    }
  }
}