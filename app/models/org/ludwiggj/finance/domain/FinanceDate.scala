package models.org.ludwiggj.finance.domain

import java.sql.Date

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class FinanceDate(val date: DateTime) {
  override def toString = date.toString(FinanceDate.formatter)

  final override def equals(other: Any) = {
    val that = other.asInstanceOf[FinanceDate]
    if (that == null) false
    else date == that.date
  }

  final override def hashCode = date.hashCode()
}

object FinanceDate {
  private val dateFormat = "dd/MM/yyyy"
  private val formatter = DateTimeFormat.forPattern(dateFormat)
  private val dateRegex = s"\\s*(\\d{2}/\\d{2}/\\d{4})\\s*".r

  def apply(stringyDate: String) = stringyDate match {
    case dateRegex(stringDate) => new FinanceDate(DateTime.parse(stringDate, formatter))
    case _ => throw new IllegalArgumentException(s"Date must be in format $dateFormat")
  }

  def apply(sqlDate: Date) = new FinanceDate(new DateTime(sqlDate))
}