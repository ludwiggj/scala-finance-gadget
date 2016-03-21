package models.org.ludwiggj.finance.builders

import com.gargoylesoftware.htmlunit.BrowserVersion
import models.org.ludwiggj.finance.web.{LoginForm, Login, WebSiteConfig, WebClient}

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
    LoginForm(WebClient(BrowserVersion.CHROME), config.get, targetPage.get);
  }
}

object LoginFormBuilder {
  def aLoginForm() = {
    new LoginFormBuilder()
  }
}