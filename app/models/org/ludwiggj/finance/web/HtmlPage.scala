package models.org.ludwiggj.finance.web

import com.gargoylesoftware.htmlunit.ElementNotFoundException
import com.gargoylesoftware.htmlunit.html.{DomNode, HtmlAnchor, HtmlForm}

import scala.collection.JavaConverters._

class HtmlPage private(private val page: HtmlUnitHtmlPage,
                       private val logoutText: Option[String]) extends HtmlEntity {

  private val logoutLink: Option[HtmlAnchor] = {
    logoutText match {
      case None => None
      case Some(anchor) => try {
        page.getAnchorByText(anchor) match {
          case link: HtmlAnchor => Some(link)
        }
      } catch {
        case ex: ElementNotFoundException =>
          throw new NotAuthenticatedException()
      }
    }
  }

  def getFirstByXPath(xpathExpr: String): DomNode = page.getFirstByXPath(xpathExpr)

  def getForms(): List[HtmlForm] = (page.getForms).asScala.toList

  def asXml: String = page.asXml

  def isLoggedIn(): Boolean = logoutLink.isDefined

  def logOff(): HtmlPage = HtmlPage(logoutLink.get.click())
}

object HtmlPage {
  def apply(page: HtmlUnitHtmlPage) = new HtmlPage(page, None)

  def apply(page: HtmlUnitHtmlPage, logoutText: String) =
    new HtmlPage(page, Some(logoutText))
}