package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.file.PersistableToFile
import org.joda.time.LocalDate

import scala.math.BigDecimal.RoundingMode

case class Price(fundName: FundName, date: LocalDate, inPounds: BigDecimal) extends PersistableToFile {
  val inPoundsScaled: BigDecimal = Price.scaled(inPounds)

  override def toString =
    s"Price [name: $fundName, date: ${FormattableLocalDate(date)}, price: £$inPoundsScaled]"

  def toFileFormat = s"$fundName$separator${FormattableLocalDate(date)}$separator$inPoundsScaled"

  override def hashCode: Int = {
    val prime = 31
    var result = 1
    result = prime * result + (if (fundName == null) 0 else fundName.hashCode)
    result = prime * result + (if (date == null) 0 else date.hashCode)
    result = prime * result + (if (inPoundsScaled == null) 0 else inPoundsScaled.hashCode)
    return result
  }

  def canEqual(p: Price): Boolean = {
    (this.fundName == p.fundName) &&
      (this.date == p.date) &&
      (this.inPoundsScaled == p.inPoundsScaled)
  }

  // TODO - Not sure hashCode comparison is needed or wise
  override def equals(that: Any): Boolean = {
    that match {
      case that: Price => that.canEqual(this) && (this.hashCode == that.hashCode)
      case _ => false
    }
  }
}

object Price {

  import models.org.ludwiggj.finance.aLocalDate

  val PriceDecimalPlaces = 4

  def apply(row: Array[String]): Price = {
    val fundName = row(0)
    val priceDate = row(1)
    val priceInPounds = aBigDecimal(row(2))

    Price(fundName, priceDate, priceInPounds)
  }

  def apply(fundNameStr: String, priceDateStr: String, priceInPounds: BigDecimal): Price = {
    val fundName = FundName(fundNameStr)
    val priceDate = aLocalDate(priceDateStr)

    Price(fundName, priceDate, priceInPounds)
  }

  def scaled(amount: BigDecimal): BigDecimal = {
    amount.setScale(PriceDecimalPlaces, RoundingMode.HALF_EVEN)
  }
}