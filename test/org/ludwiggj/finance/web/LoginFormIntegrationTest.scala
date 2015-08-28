package org.ludwiggj.finance.web

import models.org.ludwiggj.finance.builders.LoginFormBuilder
import LoginFormBuilder.aLoginForm
import models.org.ludwiggj.finance.web.{NotAuthenticatedException, WebSiteConfig}
import org.scalatest.{FunSuite, Matchers}

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