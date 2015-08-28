package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.file.PersistableToFile

case class Holding(val price: Price, val units: BigDecimal) extends PersistableToFile {
  def value = (units * price.inPounds).setScale(2, BigDecimal.RoundingMode.HALF_UP)

  def priceInPounds = price.inPounds

  def priceDate = price.date

  def priceDateAsSqlDate = price.dateAsSqlDate

  def name = price.holdingName

  override def toString =
    s"Financial Holding [name: ${price.holdingName}, units: $units, date: ${price.date}, price: £${price.inPounds}," +
      s" value: £$value]"

  def toFileFormat = s"${price.holdingName}$separator$units$separator${price.date}$separator${price.inPounds}" +
    s"$separator$value"
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
    Holding(Price(cleanHoldingName(holdingName), FinanceDate(date), priceInPounds), parseNumber(units))
  }

  def apply(row: Array[String]): Holding = {
    Holding(Price(row(0), row(2), row(3)), parseNumber(row(1)))
  }
}