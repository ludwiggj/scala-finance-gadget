package org.ludwiggj.finance.web

import org.scalatest.{Matchers, FunSuite}
import org.ludwiggj.finance.builders.LoginFormBuilder.aLoginForm

class LoginFormIntegrationTest extends FunSuite with Matchers {

  test("login form works with third party library") {
    val config = WebSiteConfig("cofunds.conf")
    val loginForm =
      aLoginForm().basedOnConfig(config).loggingIntoPage("transactions").build();

    val loginAccount = config.getAccountList()(0).name
    val loggedInPage = loginForm.loginAs(loginAccount)

    val logInText = "Log In"
    val logOffText = "Log off"

    // Log in, and verify that log off link is available
    val logOffLink = loggedInPage.getAnchorByText(logOffText)
    logOffLink.asText() should equal (logOffText)

    // Log off, and verify that log in link is available
    val loggedOffPage = HtmlPage(logOffLink.click())
    val logInLink = loggedOffPage.getFirstByXPath(s"//a[span[text()='$logInText']]")
    logInLink.asText() should equal (logInText)
  }
}