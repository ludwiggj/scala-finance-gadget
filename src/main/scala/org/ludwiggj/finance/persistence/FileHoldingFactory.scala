package org.ludwiggj.finance.persistence

import java.io.File

import scala.io.Source._
import org.ludwiggj.finance.domain.Holding

class FileHoldingFactory(private val fileName: String) extends FileFinanceRowParser {
  def getHoldings(): List[Holding] = {
    val resource =
          if (fileName.startsWith("/")) getClass.getResource(s"$fileName")
            else new File(fileName).toURI().toURL()

    val source = fromURL(resource)
    val tokenisedLines = parseRows(source)
    val holdings = (for (line <- tokenisedLines) yield Holding(line)).toList
    source.close()
    holdings
  }
}

object FileHoldingFactory {
  def apply(fileName: String) = {
    new FileHoldingFactory(fileName)
  }
}