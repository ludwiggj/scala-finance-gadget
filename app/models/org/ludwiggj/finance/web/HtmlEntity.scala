package models.org.ludwiggj.finance.web

import com.gargoylesoftware.htmlunit.html.{DomNode, HtmlForm}

trait HtmlEntity {
  def getFirstByXPath(xpathExpr: String): DomNode

  def getForms: List[HtmlForm]

  def asXml: String

  def isLoggedIn(): Boolean

  def logOff(): HtmlPage
}
