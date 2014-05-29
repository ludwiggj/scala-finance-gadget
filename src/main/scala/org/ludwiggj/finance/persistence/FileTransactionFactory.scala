package org.ludwiggj.finance.persistence

import scala.io.Source._
import org.ludwiggj.finance.domain.Transaction

class FileTransactionFactory(private val fileName: String) extends FileFinanceRowParser {
  def getTransactions(): List[Transaction] = {
    val source = fromURL(getClass.getResource(s"/$fileName"))
    val tokenisedLines = parseRows(source)
    val transactions = (for (line <- tokenisedLines) yield Transaction(line)).toList
    source.close()
    transactions
  }
}

object FileTransactionFactory {
  def apply(fileName: String) = {
    new FileTransactionFactory(fileName)
  }
}