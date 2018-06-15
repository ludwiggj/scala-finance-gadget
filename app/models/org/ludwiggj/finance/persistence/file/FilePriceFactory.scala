package models.org.ludwiggj.finance.persistence.file

import models.org.ludwiggj.finance.domain.{Price, _}
import models.org.ludwiggj.finance.aLocalDate

class FilePriceFactory private(private val priceFileName: String) extends {
  val fileName = priceFileName
} with FileFinanceRowParser {

  def fetchPrices(): List[Price] = {
    fetchLines() map (line => {
      val fundName = FundName(line(0))
      val priceDate = aLocalDate(line(1))
      val priceInPounds = aBigDecimal(line(2))

      Price(fundName, priceDate, priceInPounds)
    })
  }
}

object FilePriceFactory {
  def apply(fileName: String) = new FilePriceFactory(fileName)
}