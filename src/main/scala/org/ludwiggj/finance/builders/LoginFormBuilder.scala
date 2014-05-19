package org.ludwiggj.finance.builders

import org.ludwiggj.finance.{WebSiteConfig, WebClient, LoginForm}
import com.gargoylesoftware.htmlunit.BrowserVersion

class LoginFormBuilder {
  var config: Option[WebSiteConfig] = None
  var targetPage: Option[String] = None

  def basedOnConfig(config: WebSiteConfig) = {
    this.config = Some(config)
    this
  }

  def loggingIntoPage(targetPage: String) = {
    this.targetPage = Some(targetPage)
    this
  }

  def build() = {
    LoginForm(WebClient(BrowserVersion.FIREFOX_24), config.get, targetPage.get);
  }
}

object LoginFormBuilder {
  def aLoginForm() = {
    new LoginFormBuilder()
  }
}