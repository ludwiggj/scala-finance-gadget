package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.file.PersistableToFile
import org.joda.time.LocalDate

// Price defined as abstract case class so that the number of decimal places in the price can be restricted before
// it is passed into the case class
abstract case class Price private(fundName: FundName, date: LocalDate, inPounds: BigDecimal) extends PersistableToFile {

  override def toString =
    s"Price [name: $fundName, date: ${FormattableLocalDate(date)}, price: £$inPounds]"

  def toFileFormat = s"$fundName$separator${FormattableLocalDate(date)}$separator$inPounds"

  // Must define copy method manually
  def copy(fundName: FundName = this.fundName,
           date: LocalDate = this.date,
           inPounds: BigDecimal = this.inPounds): Price = {
    Price(fundName, date, inPounds)
  }
}

object Price {

  import models.org.ludwiggj.finance.aLocalDate

  val numberOfDecimalPlaces = 4

  def apply(fundNameStr: String, priceDateStr: String, priceInPounds: BigDecimal): Price = {
    val fundName = FundName(fundNameStr)
    val priceDate = aLocalDate(priceDateStr)

    Price(fundName, priceDate, priceInPounds)
  }

  def apply(fundName: FundName, date: LocalDate, inPounds: BigDecimal): Price = {
    new Price(fundName, date, scaled(inPounds, numberOfDecimalPlaces)){}
  }
}