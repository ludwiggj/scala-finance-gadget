package models.org.ludwiggj.finance.persistence.file

import models.org.ludwiggj.finance.domain.Holding

class FileHoldingFactory private(val userName: String, private val holdingFileName: String) extends {
  val fileName = holdingFileName
} with FileFinanceRowParser {

  def getHoldings(): List[Holding] = {
    getLines() map (Holding(userName, _))
  }
}

object FileHoldingFactory {
  def apply(userName: String, fileName: String) = {
    new FileHoldingFactory(userName, fileName)
  }
}