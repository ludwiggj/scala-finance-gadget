package models.org.ludwiggj.finance.web

import models.org.ludwiggj.finance.domain.Holding

class WebSiteHoldingFactory private(private val login: Login, private val userName: String)
  extends HtmlPageFinanceRowParser(login, userName, financeEntityTableSelector = s"table[id~=Holdings] tr") {

  def getHoldings(): List[Holding] = {
    val holdings: Iterable[Holding] = for (txRow <- getRows()) yield Holding(userName, txRow.toString)
    holdings.toList
  }
}

object WebSiteHoldingFactory {
  def apply(login: Login, userName: String) = {
    new WebSiteHoldingFactory(login, userName)
  }
}