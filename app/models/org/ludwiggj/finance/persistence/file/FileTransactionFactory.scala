package models.org.ludwiggj.finance.persistence.file

import models.org.ludwiggj.finance.domain.Transaction

class FileTransactionFactory private(val userName: String, private val transactionFileName: String) extends {
  val fileName = transactionFileName
} with FileFinanceRowParser {
  def fetchTransactions(): List[Transaction] = {
    fetchLines() map (Transaction(userName, _))
  }
}

object FileTransactionFactory {
  def apply(userName: String, fileName: String): FileTransactionFactory = {
    new FileTransactionFactory(userName, fileName)
  }
}