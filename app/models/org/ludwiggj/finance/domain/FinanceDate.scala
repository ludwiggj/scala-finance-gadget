package models.org.ludwiggj.finance.domain

import java.sql.Date
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import models.org.ludwiggj.finance.sqlDateToDateTime
import scala.language.implicitConversions

case class FinanceDate(val date: DateTime) {
  override def toString = date.toString(FinanceDate.formatter)

  final override def equals(other: Any) = {
    val that = other.asInstanceOf[FinanceDate]
    if (that == null) false
    else (date == that.date)
  }

  final override def hashCode = date.hashCode()
}

object FinanceDate {
  private val dateFormat = "dd/MM/yyyy"
  private val formatter = DateTimeFormat.forPattern(dateFormat)

  def apply(stringyDate: String): FinanceDate = {
    val DateRegex = s"\\s*(\\d{2}/\\d{2}/\\d{4})\\s*".r

    stringyDate match {
      case DateRegex(stringDate) => FinanceDate(DateTime.parse(stringDate, formatter))
      case _ => throw new IllegalArgumentException(s"Date $stringyDate must be in format $dateFormat")
    }
  }

  import models.org.ludwiggj.finance.dateTimeToSqlDate

  implicit def financeDateToSqlDate(financeDate: FinanceDate): Date = financeDate.date

  implicit def sqlDateToFinanceDate(sqlDate: Date) = FinanceDate(sqlDate)

  implicit def stringToFinanceDate(stringyDate: String) = FinanceDate(stringyDate)
}