package models.org.ludwiggj

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

import scala.language.implicitConversions

package object finance {
  val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")

  def aLocalDate(stringyDate: String): LocalDate = {
    val DateRegex = s"\\s*(\\d{2}/\\d{2}/\\d{4})\\s*".r

    stringyDate match {
      case DateRegex(stringDate) => new LocalDate(LocalDate.parse(stringDate, formatter))
      case _ => throw new IllegalArgumentException(s"Date $stringyDate must be in format dd/MM/yyyy")
    }
  }

  // See https://stackoverflow.com/questions/4677682/unable-to-provide-implicit-conversion-from-datetime-to-ordered-using-implicit-co
  implicit object LocalDateOrdering extends Ordering[LocalDate] {
    def compare(d1: LocalDate, d2: LocalDate) = d1.compareTo(d2)
  }
}