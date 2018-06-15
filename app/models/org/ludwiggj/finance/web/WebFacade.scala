package models.org.ludwiggj.finance.web

import java.net.URL

import com.gargoylesoftware.htmlunit.html.HtmlInput
import com.gargoylesoftware.htmlunit.{HttpMethod, WebRequest}
import com.typesafe.config.Config

import scala.collection.JavaConverters._

class WebFacade(val user: User, val config: Config) {
  private val baseUrl = urlConfigItem("base")
  private val loginUrl = url("login")
  private val logoutUrl = url("logout")

  private val webClient = WebClient()

  private def urlConfigItem(configItem: String): String = {
    config.getString("site.url." + configItem)
  }

  private def url(configItem: String): String = {
    s"$baseUrl${urlConfigItem(configItem)}"
  }

  def login(): Unit = {
    println(s"Logging into $loginUrl as ${user.username}")
    val html = webClient.getPage(loginUrl)
    val form = html.getForms.asScala.toList.head
    form.getInputByName("username").asInstanceOf[HtmlInput].setValueAttribute(user.username)
    form.getInputByName("password").asInstanceOf[HtmlInput].setValueAttribute(user.password)
    form.getElementsByTagName("button").get(0).click()
  }

  def logout(): Unit = {
    webClient.getPage(logoutUrl)
  }

  private def dataByPost(url: String): String = {
    val wr = new WebRequest(new URL(url), HttpMethod.POST)
    wr.setAdditionalHeader("Content-Type", "application/json; charset=utf-8")
    wr.setAdditionalHeader("Accept", "application/json, text/javascript, */*; q=0.01")
    webClient.getPage(wr)
  }

  def get(endPoint: String): String = {
    dataByPost(s"${url(endPoint)}${user.accountId}")
  }
}

object WebFacade {
  def apply(user: User, config: Config): WebFacade = {
    new WebFacade(user, config)
  }
}