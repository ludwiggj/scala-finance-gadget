package org.ludwiggj.finance.domain

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.sql.Date

class FinanceDate(val date: DateTime) {
  override def toString = date.toString(FinanceDate.formatter)
  def asSqlDate = Date.valueOf(date.toString(DateTimeFormat.forPattern("yyyy-MM-dd")))

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
}