package org.ludwiggj.finance

import com.gargoylesoftware.htmlunit.{BrowserVersion, WebClient => HtmlUnitWebClient}
import java.util.logging.Logger

class WebClient(browserVersion: BrowserVersion) {
  private val webClient = new HtmlUnitWebClient(browserVersion)

  // Suppress logging of errors
  Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF)

  def getPage(url: String): HtmlPage = HtmlPage(webClient.getPage(url))

  def setThrowExceptionOnScriptError(setting: Boolean) =
    webClient.getOptions().setThrowExceptionOnScriptError(setting);
}

object WebClient {
  def apply(browserVersion: BrowserVersion) = new WebClient(browserVersion)
}