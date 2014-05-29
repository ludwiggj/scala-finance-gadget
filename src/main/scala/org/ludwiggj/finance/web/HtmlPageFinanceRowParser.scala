package org.ludwiggj.finance.web

import org.filippodeluca.ssoup.SSoup._
import org.jsoup.nodes.Element

trait HtmlPageFinanceRowParser {
  private val headerRow = 1
  val financeEntityTableSelector: String

  def parseRows(htmlContent: HtmlEntity): Iterable[Element] = {
    val page = parse(htmlContent.asXml)
    page.select(financeEntityTableSelector) drop headerRow
  }
}