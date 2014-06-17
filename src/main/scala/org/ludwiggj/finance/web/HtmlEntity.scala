package org.ludwiggj.finance.web

import com.gargoylesoftware.htmlunit.html.{HtmlForm, DomNode}

trait HtmlEntity {
  def getFirstByXPath(xpathExpr: String): DomNode

  def getForms: List[HtmlForm]

  def asXml: String

  def isLoggedIn(): Boolean

  def logOff(): HtmlPage
}
