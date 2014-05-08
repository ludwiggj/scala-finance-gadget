package org.ludwiggj.finance

import com.gargoylesoftware.htmlunit.html.{HtmlPage => HtmlUnitHtmlPage, HtmlAnchor, DomNode}

class HtmlPage(private val page: HtmlUnitHtmlPage) {
  // For ScalaMock
//  def this() {
//    this(null)
//  }

  def getFirstByXPath(xpathExpr: String): DomNode = page.getFirstByXPath(xpathExpr)
  def getForms = page.getForms
  def asXml = page.asXml
  def getAnchorByText(text: String): HtmlAnchor = page.getAnchorByText(text)
}

object HtmlPage {
  def apply(page: HtmlUnitHtmlPage) = new HtmlPage(page)
}