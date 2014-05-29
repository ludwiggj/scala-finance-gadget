package org.ludwiggj.finance.web

import com.gargoylesoftware.htmlunit.html.{HtmlForm, HtmlAnchor, DomNode}

trait HtmlEntity {
  def getFirstByXPath(xpathExpr: String): DomNode

  def getForms: List[HtmlForm]

  def asXml: String

  def getAnchorByText(text: String): HtmlAnchor
}
