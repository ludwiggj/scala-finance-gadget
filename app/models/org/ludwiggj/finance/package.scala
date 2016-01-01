package models.org.ludwiggj

import java.sql.Date
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.language.implicitConversions

package object finance {
  implicit def dateTimeToSqlDate(dateTime: DateTime): Date =
    Date.valueOf(dateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd")))

  implicit def sqlDateToDateTime(sqlDate: Date) = new DateTime(sqlDate)

  implicit def dateTimeToDate(datetime: DateTime) = datetime.toDate

  implicit def stringToSqlDate(stringyDate: String): Date = {
    val DateRegex = """(\d{1,2})/(\d{1,2})/(\d{4})""".r

    stringyDate match {
      case DateRegex(date, month, year) => Date.valueOf(s"$year-$month-$date")
      case _ => throw new IllegalArgumentException(s"Date $stringyDate must be in format (d)d/(m)m/yyyy")
    }
  }
}