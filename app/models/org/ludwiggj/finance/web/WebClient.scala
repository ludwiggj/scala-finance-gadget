package models.org.ludwiggj.finance.web

import com.gargoylesoftware.htmlunit.{BrowserVersion, WebClient => HtmlUnitWebClient}

class WebClient private(browserVersion: BrowserVersion) {
  private val webClient = new HtmlUnitWebClient(browserVersion)

  def getPage(url: String): HtmlEntity = HtmlPage(webClient.getPage(url))

  def setThrowExceptionOnScriptError(setting: Boolean) =
    webClient.getOptions().setThrowExceptionOnScriptError(setting);
}

object WebClient {
  def apply(browserVersion: BrowserVersion) = new WebClient(browserVersion)
}