package org.ludwiggj.finance

import org.ludwiggj.finance.Transaction

// Use Login trait as parameter instead of LoginForm?
class WebSiteFinanceEntityFactory(private val loginForm: LoginForm, private val accountName: String)
  extends PageParser {

  def getTransactions(): List[Transaction] = {
    val htmlContent = loginForm.loginAs(accountName)
    val txRows = parseFinanceEntity(htmlContent)
    (for (txRow <- txRows) yield Transaction(txRow.toString)).toList
  }

  val financeEntityTableSelector: String = _
}

object WebSiteFinanceEntityFactory {
  def apply(loginForm: LoginForm, accountName: String) = {
    new WebSiteFinanceEntityFactory(loginForm, accountName)
  }
}