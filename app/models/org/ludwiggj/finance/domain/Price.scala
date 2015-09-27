package models.org.ludwiggj.finance.domain

import java.sql.Date

case class Price(fundName: FundName, val date: FinanceDate, val inPounds: BigDecimal) {

  override def toString =
    s"Price [name: $fundName, date: $date, price: £$inPounds]"
}

object Price {
  def apply(fundName: String, date: Date, inPounds: BigDecimal): Price =
    new Price(fundName, FinanceDate(date), inPounds)

  def apply(row: Array[String]): Price = {
    Price(row(0), FinanceDate(row(1)), parseNumber(row(2)))
  }

  def apply(fundName: String, date: String, inPounds: String): Price = {
    Price(Array(fundName, date, inPounds))
  }
}