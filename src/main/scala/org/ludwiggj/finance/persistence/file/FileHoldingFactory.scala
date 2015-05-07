package org.ludwiggj.finance.persistence.file

import org.ludwiggj.finance.domain.Holding

class FileHoldingFactory(private val holdingFileName: String) extends {
  val fileName = holdingFileName
} with FileFinanceRowParser {

  def getHoldings(): List[Holding] = {
    getLines() map (Holding(_))
  }
}

object FileHoldingFactory {
  def apply(fileName: String) = {
    new FileHoldingFactory(fileName)
  }
}