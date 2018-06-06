package models.org.ludwiggj.finance.web

import models.org.ludwiggj.finance.domain.Transaction

class WebSiteTransactionFactory private(private val login: Login, private val userName: String)
  extends HtmlPageFinanceRowParser(login,
    userName,
    financeEntityTableSelector = s"table[id~=dgTransactions] tr") {

  def getTransactions(): List[Transaction] = {
    val txs: Iterable[Transaction] = for (txRow <- getRows()) yield Transaction(userName, txRow.toString)
    txs.toList
  }
}

object WebSiteTransactionFactory {
  def apply(login: Login, userName: String) = {
    new WebSiteTransactionFactory(login, userName)
  }
}