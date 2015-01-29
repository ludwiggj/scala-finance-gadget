package org.ludwiggj.finance.domain

import org.ludwiggj.finance.persistence.Persistable

class Holding(val name: String, val units: BigDecimal,
              val priceDate: FinanceDate, val priceInPence: BigDecimal) extends Persistable {
  def value = (units * (priceInPence / 100)).setScale(2, BigDecimal.RoundingMode.HALF_UP)

  def dateAsSqlDate = priceDate.asSqlDate

  override def toString =
    s"Financial Holding [name: $name, units: $units, date: ${priceDate}, price: $priceInPence, value: £$value]"

  def toFileFormat = s"$name$separator$units$separator$priceDate$separator$priceInPence$separator$value"

  final override def equals(other: Any) = {
    val that = if (other.isInstanceOf[Holding]) other.asInstanceOf[Holding] else null
    if (that == null) false
    else {
      val objectsEqual =
        (
          (name == that.name) &&
            (units == that.units) &&
            (priceDate == that.priceDate) &&
            (priceInPence == priceInPence)
          )
      objectsEqual
    }
  }

  final override def hashCode = {
    var result = 17
    result = 31 * result + name.hashCode
    result = 31 * result + units.hashCode
    result = 31 * result + priceDate.hashCode
    result = 31 * result + priceInPence.hashCode
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
    Holding(cleanHoldingName(holdingName), parseNumber(units), FinanceDate(date), parseNumber(priceInPence))
  }

  def apply(row: Array[String]): Holding = {
      Holding(row(0), parseNumber(row(1)), FinanceDate(row(2)), parseNumber(row(3)))
  }
}