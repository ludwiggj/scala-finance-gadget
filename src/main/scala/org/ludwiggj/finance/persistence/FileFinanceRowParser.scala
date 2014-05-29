package org.ludwiggj.finance.persistence

import scala.io.Source
import org.ludwiggj.finance.domain.separator

trait FileFinanceRowParser {
  def parseRows(source: Source): Iterator[Array[String]] = {
    val lineIterator = source.getLines
    for (l <- lineIterator) yield l.split(separator)
  }
}