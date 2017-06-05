package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.formatter
import org.joda.time.LocalDate

import scala.language.implicitConversions

// Explicit way of overriding toString functionality of LocalDate
// Cannot mix in a trait (as per e.g.
// https://stackoverflow.com/questions/28730672/in-scala-can-i-override-java-calendars-tostring-and-have-a-constructor-with-a
// ) because LocalDate is declared final.
case class FormattableLocalDate(date: LocalDate) {
  override def toString = date.toString(formatter)
}