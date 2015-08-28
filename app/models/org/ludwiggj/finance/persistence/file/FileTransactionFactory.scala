package models.org.ludwiggj.finance.persistence.file

import models.org.ludwiggj.finance.domain.Transaction

class FileTransactionFactory(private val transactionFileName: String) extends {
  val fileName = transactionFileName
} with FileFinanceRowParser {
  def getTransactions(): List[Transaction] = {
    getLines() map (Transaction(_))
  }
}

object FileTransactionFactory {
  def apply(fileName: String) = {
    new FileTransactionFactory(fileName)
  }
}