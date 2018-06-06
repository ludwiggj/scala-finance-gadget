package models.org.ludwiggj.finance.web

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

class WebSiteConfig private(private val config: Config) {
  def getUrlForPage(targetPage: String): String = {
    config.getString(s"site.baseUrl.$targetPage")
  }

  def getLoginFormFields(): List[FormField] = {
    (config.getConfigList("site.login.form.fields").asScala map (FormField(_))).toList
  }

  def getSubmitButton(): String = {
    config.getString("site.login.form.submit")
  }

  def getUserList(): List[User] = {
    (config.getConfigList("site.userAccounts").asScala map (User(_))).toList
  }

  def getLoginText(): String = {
    (config.getString("site.login.text"))
  }

  def getLogoutText(): String = {
    (config.getString("site.logout.text"))
  }
}

object WebSiteConfig {
  def apply(configFileName: String): WebSiteConfig = new WebSiteConfig(ConfigFactory.load(configFileName))
}