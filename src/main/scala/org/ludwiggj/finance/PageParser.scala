package org.ludwiggj.finance

import org.filippodeluca.ssoup.SSoup._
import org.jsoup.nodes.Element

trait PageParser {
  private val headerRow = 1
  private val transactionTableSelector = s"table[id~=dgTransactions] tr"

  def parseTransactions(htmlContent: HtmlPage): Iterable[Element] = {
    val page = parse(htmlContent.asXml)
    page.select(transactionTableSelector) drop headerRow
  }
}