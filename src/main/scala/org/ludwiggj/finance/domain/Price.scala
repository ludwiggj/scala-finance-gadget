package org.ludwiggj.finance.domain

import java.sql.Date

case class Price(val holdingName: String, val date: FinanceDate, val inPounds: BigDecimal) {

  def dateAsSqlDate = date.asSqlDate

  override def toString =
    s"Price [name: $holdingName, date: $date, price: £$inPounds]"
}

object Price {
  def apply(holdingName: String, date: Date, inPounds: BigDecimal): Price =
    new Price(holdingName, FinanceDate(date), inPounds)

  def apply(row: Array[String]): Price = {
    Price(row(0), FinanceDate(row(1)), parseNumber(row(2)))
  }

  def apply(holdingName: String, date: String, inPounds: String): Price = {
    Price(Array(holdingName, date, inPounds))
  }
}