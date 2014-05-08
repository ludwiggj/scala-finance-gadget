 package org.ludwiggj.finance

import com.typesafe.config.Config
import com.gargoylesoftware.htmlunit.html.{HtmlSubmitInput, HtmlInput, HtmlForm}
import scala.collection.JavaConversions._

class LoginForm(private val webClient: WebClient,
                private val baseUrl: String,
                private val fields: List[FormField],
                private val submitButton: String
                 ) extends Login {

  def login(account: Account): HtmlPage = {
    // Carry on if we get a javascript error
    webClient.setThrowExceptionOnScriptError(false);
    val page: HtmlPage = webClient.getPage(baseUrl)
    val form: HtmlForm = (page.getForms).get(0)

    fields foreach {
      f => form.getInputByName(f.htmlName).asInstanceOf[HtmlInput].setValueAttribute(account.attributeValue(f.name))
    }

    HtmlPage(form.getInputByName(submitButton).asInstanceOf[HtmlSubmitInput].click())
  }
}

object LoginForm {
  def apply(webClient: WebClient, config: Config, targetPage: String) = {
    new LoginForm(
      webClient,
      config.getString(s"site.baseUrl.$targetPage"),
      (config.getConfigList("site.login.form.fields") map (FormField(_))).toList,
      config.getString("site.login.form.submit")
    )
  }
}