package org.ludwiggj.finance.domain

class Holding(val name: String, val units: BigDecimal, val priceDate: FinanceDate, priceInPence: BigDecimal) {
  val priceInPounds = priceInPence / 100

  def value = (units * priceInPounds).setScale(2, BigDecimal.RoundingMode.HALF_UP)

  override def toString =
    s"Financial Holding [name: $name, units: $units, price: £$priceInPounds, date: ${priceDate}, value: £$value]"

  final override def equals(other: Any) = {
    val that = if (other.isInstanceOf[Holding]) other.asInstanceOf[Holding] else null
    if (that == null) false
    else {
      val objectsEqual =
        (
          (name == that.name) &&
            (units == that.units) &&
            (priceDate == that.priceDate) &&
            (priceInPounds == that.priceInPounds)
          )
      objectsEqual
    }
  }

  final override def hashCode = {
    var result = 17
    result = 31 * result + name.hashCode
    result = 31 * result + units.hashCode
    result = 31 * result + priceDate.hashCode
    result = 31 * result + priceInPounds.hashCode
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
}