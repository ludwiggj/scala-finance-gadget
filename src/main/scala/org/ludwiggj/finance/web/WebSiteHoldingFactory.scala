package org.ludwiggj.finance.web

import org.ludwiggj.finance.builders.LoginFormBuilder
import org.ludwiggj.finance.domain.Holding

class WebSiteHoldingFactory(private val loginFormBuilder: LoginFormBuilder, private val accountName: String)
  extends {
      val financeEntityTableSelector = s"table[id~=Holdings] tr"
  }
  with HtmlPageFinanceRowParser {

  def getHoldings(): List[Holding] = {
    val loginForm = loginFormBuilder.loggingIntoPage("valuations").build()
    val loggedInPage = loginForm.loginAs(accountName)
    val txRows = parseRows(loggedInPage)
    loggedInPage.logOff()
    (for (txRow <- txRows) yield Holding(txRow.toString)).toList
  }
}

object WebSiteHoldingFactory {
  def apply(loginFormBuilder: LoginFormBuilder, accountName: String) = {
    new WebSiteHoldingFactory(loginFormBuilder, accountName)
  }
}