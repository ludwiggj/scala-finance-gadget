package models.org.ludwiggj.finance.web

import models.org.ludwiggj.finance.builders.LoginFormBuilder
import models.org.ludwiggj.finance.domain.Holding

class WebSiteHoldingFactory private(private val loginFormBuilder: LoginFormBuilder, private val userName: String)
  extends {
    val financeEntityTableSelector = s"table[id~=Holdings] tr"
  }
  with HtmlPageFinanceRowParser {

  def getHoldings(): List[Holding] = {
    val loginForm = loginFormBuilder.loggingIntoPage("valuations").build()
    val loggedInPage = loginForm.loginAs(userName)
    val txRows = parseRows(loggedInPage)
    loggedInPage.logOff()
    (for (txRow <- txRows) yield Holding(userName, txRow.toString)).toList
  }
}

object WebSiteHoldingFactory {
  def apply(loginFormBuilder: LoginFormBuilder, userName: String) = {
    new WebSiteHoldingFactory(loginFormBuilder, userName)
  }
}