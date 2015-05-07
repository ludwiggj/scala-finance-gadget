package org.ludwiggj.finance.domain

case class Price(val holdingName: String, val date: FinanceDate, val inPounds: BigDecimal) {

  def dateAsSqlDate = date.asSqlDate

  override def toString =
    s"Price [name: $holdingName, date: $date, price: £$inPounds]"
}

object Price {
  def apply(row: Array[String]): Price = {
    Price(row(0), FinanceDate(row(1)), parseNumber(row(2)))
  }
}