package org.ludwiggj.finance

import com.gargoylesoftware.htmlunit.html.{HtmlSubmitInput, HtmlInput, HtmlForm}

class LoginForm(private val webClient: WebClient,
                private val baseUrl: String,
                private val fields: List[FormField],
                private val submitButton: String,
                private val accounts: List[Account]
                 ) extends Login {

  def accountByName = Map((for (acc <- accounts) yield (acc.name, acc)): _*)

  def loginAs(accountName: String) = {
    loginAs(accountByName(accountName))
  }

  def loginWithAccountAtIndex(accountIndex: Integer) = {
    loginAs(accounts(accountIndex))
  }

  def loginAs(loginAccount: Account): HtmlPage = {
    // Carry on if we get a javascript error
    webClient.setThrowExceptionOnScriptError(false);
    val page: HtmlPage = webClient.getPage(baseUrl)
    val form: HtmlForm = (page.getForms).get(0)

    fields foreach {
      f => form.getInputByName(f.htmlName).asInstanceOf[HtmlInput].setValueAttribute(loginAccount.attributeValue(f.name))
    }

    println(s"Logging in to $baseUrl as ${loginAccount.name}")

    HtmlPage(form.getInputByName(submitButton).asInstanceOf[HtmlSubmitInput].click())
  }
}

object LoginForm {
  def apply(webClient: WebClient, config: WebSiteConfig, targetPage: String) = {
    new LoginForm(
      webClient,
      config.getUrlForPage(targetPage),
      config.getLoginFormFields(),
      config.getSubmitButton(),
      config.getAccountList()
    )
  }
}