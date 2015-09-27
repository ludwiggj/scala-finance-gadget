package models.org.ludwiggj.finance.web

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConversions._

class WebSiteConfig private(private val config: Config) {
  def getUrlForPage(targetPage: String) = {
    config.getString(s"site.baseUrl.$targetPage")
  }

  def getLoginFormFields() = {
    (config.getConfigList("site.login.form.fields") map (FormField(_))).toList
  }

  def getSubmitButton() = {
    config.getString("site.login.form.submit")
  }

  def getUserList() = {
    (config.getConfigList("site.userAccounts") map (User(_))).toList
  }

  def getLoginText() = {
    (config.getString("site.login.text"))
  }

  def getLogoutText() = {
    (config.getString("site.logout.text"))
  }
}

object WebSiteConfig {
  def apply(configFileName: String) = new WebSiteConfig(ConfigFactory.load(configFileName))
}