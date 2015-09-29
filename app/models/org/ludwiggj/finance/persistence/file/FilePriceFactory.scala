package models.org.ludwiggj.finance.persistence.file

import models.org.ludwiggj.finance.domain._

class FilePriceFactory private(private val priceFileName: String) extends {
  val fileName = priceFileName
} with FileFinanceRowParser {

  def getPrices(): List[Price] = {
    getLines() map (line => Price(line(0), line(1), line(2)))
  }
}

object FilePriceFactory {
  def apply(fileName: String) = new FilePriceFactory(fileName)
}