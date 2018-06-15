package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.file.PersistableToFile
import org.joda.time.LocalDate

case class Holding(userName: String, price: Price, units: BigDecimal) extends PersistableToFile with Ordered[Holding] {
  def value: BigDecimal = (units * price.inPounds).setScale(2, BigDecimal.RoundingMode.HALF_UP)

  def priceInPounds: BigDecimal = price.inPounds

  def priceDate: LocalDate = price.date

  def name: FundName = price.fundName

  override def toString: String =
    s"Financial Holding [userName: $userName, name: ${price.fundName}, units: $units, date: ${FormattableLocalDate(price.date)}, " +
      s"price: £${price.inPounds}, value: £$value]"

  def toFileFormat: String = s"${price.fundName}$separator$units$separator${FormattableLocalDate(price.date)}$separator${price.inPounds}" +
    s"$separator$value"

  def canEqual(h: Holding): Boolean = (userName == h.userName) && (name == h.name) && (priceDate == h.priceDate)

  override def equals(that: Any): Boolean =
    that match {
      case that: Holding => that.canEqual(this) && (this.hashCode == that.hashCode)
      case _ => false
    }

  override def hashCode: Int = {
    val prime = 31
    var result = 1
    result = prime * result + (if (units == null) 0 else units.intValue())
    result = prime * result + (if (userName == null) 0 else userName.hashCode)
    result = prime * result + (if (name == null) 0 else name.hashCode)
    result = prime * result + (if (priceDate == null) 0 else priceDate.hashCode)
    result
  }

  override def compare(that: Holding): Int = this.name.compare(that.name)
}

object Holding {
  def apply(userName: String, row: Array[String]): Holding = {
    val fundName = row(0)
    val date = row(2)
    val inPounds = aBigDecimal(row(3))
    val units = aBigDecimal(row(1))

    Holding(userName, Price(fundName, date, inPounds), units)
  }
}