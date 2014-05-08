package org.ludwiggj.finance

import java.util.Date
import java.text.SimpleDateFormat

class FinanceDate(val date: Date) {
  private val displayFormat = new SimpleDateFormat("dd/MM/yy");
  override def toString = displayFormat.format(date)

  final override def equals(other: Any) = {
    val that = other.asInstanceOf[FinanceDate]
    if (that == null) false
    else date == that.date
  }

  final override def hashCode = date.hashCode()
}

object FinanceDate {
  private val parsingDateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy")
  def apply(stringyDate: String) = new FinanceDate(parsingDateFormat.parse(stringyDate))
}