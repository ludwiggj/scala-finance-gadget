package models.org.ludwiggj.finance.persistence.file

import models.org.ludwiggj.finance.domain.Holding

class FileHoldingFactory private(val userName: String, private val holdingFileName: String) extends {
  val fileName = holdingFileName
} with FileFinanceRowParser {

  def fetchHoldings(): List[Holding] = {
    fetchLines() map (Holding(userName, _))
  }
}

object FileHoldingFactory {
  def apply(userName: String, fileName: String): FileHoldingFactory = {
    new FileHoldingFactory(userName, fileName)
  }
}