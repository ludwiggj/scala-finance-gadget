package org.ludwiggj.finance.web

import org.scalatest.{Matchers, FunSuite}
import org.ludwiggj.finance.builders.LoginFormBuilder.aLoginForm

class LoginFormIntegrationTest extends FunSuite with Matchers {

  test("login form works with third party library") {
    val config = WebSiteConfig("cofunds.conf")
    val loginForm =
      aLoginForm().basedOnConfig(config).loggingIntoPage("transactions").build();

    val loginAccount = config.getAccountList()(0).name

    // Log in, and verify that log off link is available
    val loggedInPage = loginForm.loginAs(loginAccount)
    loggedInPage.isLoggedIn() should equal(true)
    println("Logged in")

    // Log off, and verify that log in link is available
    val loggedOffPage = loggedInPage.logOff()
    val loginText = config.getLoginText()

    val logInLink = loggedOffPage.getFirstByXPath(s"//a[span[text()='$loginText']]")
    logInLink.asText() should equal(loginText)

    println("Logged out")
  }

  test("NotAuthenticatedException thrown if cannot log into target page") {
    intercept[NotAuthenticatedException] {
      val config = WebSiteConfig("cofundsWithAccountWithIncorrectPassword.conf")
      val loginForm =
        aLoginForm().basedOnConfig(config).loggingIntoPage("transactions").build();

      val loginAccount = config.getAccountList()(0).name

      loginForm.loginAs(loginAccount)
    }
  }
}