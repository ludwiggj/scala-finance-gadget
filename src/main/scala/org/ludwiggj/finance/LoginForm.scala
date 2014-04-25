package org.ludwiggj.finance

import com.typesafe.config.Config
import com.gargoylesoftware.htmlunit.{BrowserVersion, WebClient}
import com.gargoylesoftware.htmlunit.html.{HtmlSubmitInput, HtmlInput, HtmlForm, HtmlPage}
import scala.collection.JavaConversions._
import scala.collection.mutable.Buffer

class LoginForm(private val baseUrl: String,
                private val fields: List[FormField],
                private val submitButton: String
                 ) {
  private val webClient = new WebClient(BrowserVersion.FIREFOX_24)

  def login(account: Account): HtmlPage = {
    val page: HtmlPage = webClient.getPage(baseUrl)
    val form: HtmlForm = (page.getForms).get(0)

    fields foreach {
      f => form.getInputByName(f.htmlName).asInstanceOf[HtmlInput].setValueAttribute(account.attributeValue(f.name))
    }

    form.getInputByName(submitButton).asInstanceOf[HtmlSubmitInput].click()
  }
}

object LoginForm {
  def apply(config: Config, targetPage: String) = {
    new LoginForm(
      config.getString(s"site.baseUrl.$targetPage"),
      (config.getConfigList("site.login.form.fields") map (FormField(_))).toList,
      config.getString("site.login.form.submit")
    )
  }
}