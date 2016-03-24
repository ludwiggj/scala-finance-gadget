package models.org.ludwiggj.finance.web

import models.org.ludwiggj.finance.Transaction
import models.org.ludwiggj.finance.builders.LoginFormBuilder

class WebSiteTransactionFactory private(private val loginFormBuilder: LoginFormBuilder, private val userName: String)
  extends {
    val financeEntityTableSelector = s"table[id~=dgTransactions] tr"
  } with HtmlPageFinanceRowParser {

  def getTransactions(): List[Transaction] = {
    val loginForm = loginFormBuilder.loggingIntoPage("transactions").build()
    val loggedInPage = loginForm.loginAs(userName)
    val txRows = parseRows(loggedInPage)
    loggedInPage.logOff()
    (for (txRow <- txRows) yield Transaction(userName, txRow.toString)).toList
  }
}

object WebSiteTransactionFactory {
  def apply(loginFormBuilder: LoginFormBuilder, userName: String) = {
    new WebSiteTransactionFactory(loginFormBuilder, userName)
  }
}