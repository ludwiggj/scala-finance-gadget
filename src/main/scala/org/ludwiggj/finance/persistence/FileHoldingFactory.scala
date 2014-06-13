package org.ludwiggj.finance.persistence

import scala.io.Source._
import org.ludwiggj.finance.domain.Holding

class FileHoldingFactory(private val fileName: String) extends FileFinanceRowParser {
  def getHoldings(): List[Holding] = {
    val source = fromURL(getClass.getResource(s"/$fileName"))
    val tokenisedLines = parseRows(source)
    val holdings = (for (line <- tokenisedLines) yield Holding(line)).toList
    source.close()
    holdings
  }
}

object FileHoldingFactory {
  def apply(fileName: String) = {
    new FileTransactionFactory(fileName)
  }
}