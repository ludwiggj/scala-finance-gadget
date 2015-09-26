package models.org.ludwiggj.finance.domain

import java.sql.Date

case class Price(val fundName: String, val date: FinanceDate, val inPounds: BigDecimal) {

  override def toString =
    s"Price [name: $fundName, date: $date, price: £$inPounds]"
}

object Price {
  def apply(fundName: String, date: Date, inPounds: BigDecimal): Price =
    new Price(cleanFundName(fundName), FinanceDate(date), inPounds)

  private def cleanFundName(name: String) = name.replaceAll("&amp;", "&").replaceAll("\\^", "").trim

  def apply(row: Array[String]): Price = {
    Price(row(0), FinanceDate(row(1)), parseNumber(row(2)))
  }

  def apply(fundName: String, date: String, inPounds: String): Price = {
    Price(Array(fundName, date, inPounds))
  }
}