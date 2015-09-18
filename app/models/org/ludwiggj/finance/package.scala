package models.org.ludwiggj

import java.sql.Date

import models.org.ludwiggj.finance.domain.FinanceDate
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.language.implicitConversions

package object finance {
  implicit def asSqlDate(dateTime: DateTime): Date =
    Date.valueOf(dateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd")))

  implicit def asSqlDate(financeDate: FinanceDate): Date = financeDate.date
}
