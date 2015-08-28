package models.org.ludwiggj.finance.persistence.file

import models.org.ludwiggj.finance.domain.Price

class FilePriceFactory(private val priceFileName: String) extends {
  val fileName = priceFileName
} with FileFinanceRowParser {

  def getPrices(): List[Price] = {
    getLines() map (Price(_))
  }
}

object FilePriceFactory {
  def apply(fileName: String) = {
    new FilePriceFactory(fileName)
  }
}