package org.ludwiggj.finance.domain

import org.ludwiggj.finance.persistence.Persistable

class Holding(val name: String, val units: BigDecimal,
              val priceDate: FinanceDate, val price: BigDecimal) extends Persistable {
  def value = (units * price).setScale(2, BigDecimal.RoundingMode.HALF_UP)

  def dateAsSqlDate = priceDate.asSqlDate

  override def toString =
    s"Financial Holding [name: $name, units: $units, date: ${priceDate}, price: £$price, value: £$value]"

  def toFileFormat = s"$name$separator$units$separator$priceDate$separator$price$separator$value"

  final override def equals(other: Any) = {
    val that = if (other.isInstanceOf[Holding]) other.asInstanceOf[Holding] else null
    if (that == null) false
    else {
      val objectsEqual =
        (
          (name == that.name) &&
            (units == that.units) &&
            (priceDate == that.priceDate) &&
            (price == price)
          )
      objectsEqual
    }
  }

  final override def hashCode = {
    var result = 17
    result = 31 * result + name.hashCode
    result = 31 * result + units.hashCode
    result = 31 * result + priceDate.hashCode
    result = 31 * result + price.hashCode
    result
  }
}

object Holding {
  def apply(name: String, units: BigDecimal, priceDate: FinanceDate, priceInPence: BigDecimal) =
    new Holding(name, units, priceDate, priceInPence)

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