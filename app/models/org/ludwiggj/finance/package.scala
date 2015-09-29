package models.org.ludwiggj

import java.sql.Date

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.language.implicitConversions

package object finance {
  implicit def dateTimeToSqlDate(dateTime: DateTime): Date =
    Date.valueOf(dateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd")))

  implicit def sqlDateToDateTime(sqlDate: Date) = new DateTime(sqlDate)
}