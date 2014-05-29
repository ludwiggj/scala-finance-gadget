package org.ludwiggj.finance.builders

import com.gargoylesoftware.htmlunit.BrowserVersion
import org.ludwiggj.finance.web.{WebSiteConfig, LoginForm, Login, WebClient}

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

  def build(): Login = {
    LoginForm(WebClient(BrowserVersion.FIREFOX_24), config.get, targetPage.get);
  }
}

object LoginFormBuilder {
  def aLoginForm() = {
    new LoginFormBuilder()
  }
}