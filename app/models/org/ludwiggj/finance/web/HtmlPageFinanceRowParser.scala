package models.org.ludwiggj.finance.web

import org.filippodeluca.ssoup.SSoup._
import org.jsoup.nodes.Element

abstract class HtmlPageFinanceRowParser(loginForm: Login,
                                        userName: String,
                                        financeEntityTableSelector: String) {
  private val headerRow = 1

  private def parseRows(htmlContent: HtmlEntity): Iterable[Element] = {
    val page = parse(htmlContent.asXml)
    page.select(financeEntityTableSelector) drop headerRow
  }

  def getRows(): Iterable[Element] = {
    val loggedInPage: HtmlEntity = loginForm.loginAs(userName)
    val txRows: Iterable[Element] = parseRows(loggedInPage)
    loggedInPage.logOff()
    txRows
  }
}