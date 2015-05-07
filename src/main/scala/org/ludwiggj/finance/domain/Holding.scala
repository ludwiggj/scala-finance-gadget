package org.ludwiggj.finance.domain

import org.ludwiggj.finance.persistence.file.PersistableToFile

case class Holding(val name: String, val units: BigDecimal,
              val priceDate: FinanceDate, val priceInPounds: BigDecimal) extends PersistableToFile {
  def value = (units * priceInPounds).setScale(2, BigDecimal.RoundingMode.HALF_UP)

  def priceDateAsSqlDate = priceDate.asSqlDate

  override def toString =
    s"Financial Holding [name: $name, units: $units, date: $priceDate, price: £$priceInPounds, value: £$value]"

  def toFileFormat = s"$name$separator$units$separator$priceDate$separator$priceInPounds$separator$value"
}

object Holding {

  def apply(row: String): Holding = {
    val holdingPattern = (
      """.*?<span.*?>([^<]+)</span>""" +
        """.*?<td[^>]*>(.*?)</td>""" +
        """.*?<td[^>]*>(.*?)</td>""" +
        """.*?<td[^>]*>(.*?)</td>""" +
        """.*?<td[^>]*>(.*?)</td>""" +
        """.*?"""
      ).r

    val holdingPattern(holdingName, units, date, priceInPence, _) = stripAllWhitespaceExceptSpace(row)
    val priceInPounds = parseNumber(priceInPence) / 100;
    Holding(cleanHoldingName(holdingName), parseNumber(units), FinanceDate(date), priceInPounds)
  }

  def apply(row: Array[String]): Holding = {
      Holding(row(0), parseNumber(row(1)), FinanceDate(row(2)), parseNumber(row(3)))
  }
}