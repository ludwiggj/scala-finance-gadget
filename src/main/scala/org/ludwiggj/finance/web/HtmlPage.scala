package org.ludwiggj.finance.web

import com.gargoylesoftware.htmlunit.html.{HtmlPage => HtmlUnitHtmlPage, HtmlForm, HtmlAnchor, DomNode}
import scala.collection.JavaConverters._

class HtmlPage(private val page: HtmlUnitHtmlPage) extends HtmlEntity {
  def getFirstByXPath(xpathExpr: String): DomNode = page.getFirstByXPath(xpathExpr)

  def getForms(): List[HtmlForm] = (page.getForms).asScala.toList

  def asXml: String = page.asXml

  def getAnchorByText(text: String): HtmlAnchor = page.getAnchorByText(text)
}

object HtmlPage {
  def apply(page: HtmlUnitHtmlPage) = new HtmlPage(page)
}