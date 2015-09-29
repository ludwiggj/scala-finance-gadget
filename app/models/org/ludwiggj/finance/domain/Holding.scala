package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.file.PersistableToFile

case class Holding(val userName: String, val price: Price, val units: BigDecimal) extends PersistableToFile {
  def value = (units * price.inPounds).setScale(2, BigDecimal.RoundingMode.HALF_UP)

  def priceInPounds = price.inPounds

  def priceDate = price.date

  def name = price.fundName

  override def toString =
    s"Financial Holding [userName: ${userName}, name: ${price.fundName}, units: $units, date: ${price.date}, " +
      s"price: £${price.inPounds}, value: £$value]"

  def toFileFormat = s"${price.fundName}$separator$units$separator${price.date}$separator${price.inPounds}" +
    s"$separator$value"

  def canEqual(h: Holding) = (userName == h.userName) && (name == h.name) && (priceDate == h.priceDate)

  override def equals(that: Any): Boolean =
    that match {
      case that: Holding => that.canEqual(this) && (this.hashCode == that.hashCode)
      case _ => false
    }

  override def hashCode: Int = {
    val prime = 31
    var result = 1
    result = prime * result + units.intValue();
    result = prime * result + (if (userName == null) 0 else userName.hashCode)
    result = prime * result + (if (name == null) 0 else name.hashCode)
    result = prime * result + (if (priceDate == null) 0 else priceDate.hashCode)
    return result
  }
}

object Holding {

  import models.org.ludwiggj.finance.domain.stringToBigDecimal
  import models.org.ludwiggj.finance.domain.FinanceDate.stringToFinanceDate

  def apply(userName: String, row: String): Holding = {
    val holdingPattern = (
      """.*?<span.*?>([^<]+)</span>""" +
        """.*?<td[^>]*>(.*?)</td>""" +
        """.*?<td[^>]*>(.*?)</td>""" +
        """.*?<td[^>]*>(.*?)</td>""" +
        """.*?<td[^>]*>(.*?)</td>""" +
        """.*?"""
      ).r

    val holdingPattern(fundName, units, date, priceInPence, _) = stripAllWhitespaceExceptSpace(row)
    val priceInPounds: BigDecimal = priceInPence / 100;
    Holding(userName, Price(fundName, date, priceInPounds), units)
  }

  import FinanceDate.stringToFinanceDate

  def apply(userName: String, row: Array[String]): Holding = {
    Holding(userName, Price(row(0), row(2), row(3)), row(1))
  }
}