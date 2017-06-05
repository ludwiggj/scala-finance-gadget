package models.org.ludwiggj

import java.sql.Date
import org.joda.time.DateTimeFieldType.{dayOfMonth, monthOfYear, year}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, LocalDate}
import scala.language.implicitConversions

package object finance {
  implicit def dateTimeToSqlDate(dateTime: DateTime): Date =
    Date.valueOf(dateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd")))

  implicit def sqlDateToLocalDate(sqlDate: Date) = new LocalDate(sqlDate)

  implicit def localDateToSqlDate(date: LocalDate) =
    Date.valueOf(s"${date.get(year())}-${date.get(monthOfYear())}-${date.get(dayOfMonth())}")

  implicit def dateTimeToDate(datetime: DateTime) = datetime.toDate

  implicit def stringToSqlDate(stringyDate: String): Date = {
    val DateRegex = """(\d{1,2})/(\d{1,2})/(\d{4})""".r

    stringyDate match {
      case DateRegex(date, month, year) => Date.valueOf(s"$year-$month-$date")
      case _ => throw new IllegalArgumentException(s"Date $stringyDate must be in format (d)d/(m)m/yyyy")
    }
  }

  val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")

  implicit def stringToLocalDate(stringyDate: String): LocalDate = {
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