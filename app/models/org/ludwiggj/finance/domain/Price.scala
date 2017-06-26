package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.file.PersistableToFile
import org.joda.time.LocalDate

case class Price(fundName: FundName, date: LocalDate, inPounds: BigDecimal) extends PersistableToFile {

  override def toString =
    s"Price [name: $fundName, date: ${FormattableLocalDate(date)}, price: £$inPounds]"

  def toFileFormat = s"$fundName$separator${FormattableLocalDate(date)}$separator$inPounds"
}

object Price {

  import models.org.ludwiggj.finance.aLocalDate

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
}