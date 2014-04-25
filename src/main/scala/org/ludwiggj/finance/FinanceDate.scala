package org.ludwiggj.finance

import java.util.Date
import java.text.SimpleDateFormat

class FinanceDate(val date: Date) {
  private val displayFormat = new SimpleDateFormat("dd/MM/yy");
  override def toString = displayFormat.format(date)
}

object FinanceDate {
  private val parsingDateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy")
  def apply(stringyDate: String) = new FinanceDate(parsingDateFormat.parse(stringyDate))
}