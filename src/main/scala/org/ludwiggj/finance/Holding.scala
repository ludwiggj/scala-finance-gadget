package org.ludwiggj.finance

import scala.util.matching.Regex

class Holding(val name: String, val units: BigDecimal, val priceDate: FinanceDate, priceInPence: BigDecimal) {
  val priceInPounds = priceInPence / 100
  def value = (units * priceInPounds).setScale(2, BigDecimal.RoundingMode.HALF_UP)

  override def toString =
    s"Financial Holding [name: $name, units: $units, price: £$priceInPounds, date: ${priceDate}, value: £$value]"
}

object Holding {
  def apply(name: String, units: BigDecimal, priceDate: FinanceDate, price: BigDecimal)  =
    new Holding(name, units, priceDate, price)

  def apply(row: String): Holding = {
    val holdingPattern = (
      """.*?<span.*?>([^<]+)</span>""" +
      """.*?<td[^>]*>(.*?)</td>""" +
      """.*?<td[^>]*>(.*?)</td>""" +
      """.*?<td[^>]*>(.*?)</td>""" +
      """.*?<td[^>]*>(.*?)</td>""" +
      """.*?"""
    ).r

    val holdingPattern(holdingName, units, date, price, _) = stripAllWhitespaceExceptSpace(row)
    Holding(cleanHoldingName(holdingName), parseNumber(units), FinanceDate(date), parseNumber(price))
  }
}