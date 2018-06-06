package models.org.ludwiggj.finance.web

import org.scalatest.{FunSuite, Ignore, Matchers}

// TODO - These tests will only pass when live config is available (which is
// TODO - obviously not shared via Git)
@Ignore
class LoginFormIntegrationTest extends FunSuite with Matchers {

  test("login form works with third party library") {
    val config = WebSiteConfig("acme")
    val loginForm = LoginForm(config, targetPage = "transactions")

    val userName = config.getUserList()(0).name

    // Log in, and verify that log off link is available
    val loggedInPage = loginForm.loginAs(userName)
    loggedInPage.isLoggedIn() should equal(true)

    // Log off, and verify that log in link is available
    val loggedOffPage = loggedInPage.logOff()
    val loginText = config.getLoginText()

    val logInLink = loggedOffPage.getFirstByXPath(s"//a[span[text()='$loginText']]")
    logInLink.asText() should equal(loginText)
  }

  test("NotAuthenticatedException thrown if cannot log into target page") {
    intercept[NotAuthenticatedException] {
      val config = WebSiteConfig("acmeWithUserWithIncorrectPassword")
      val loginForm = LoginForm(config, targetPage = "transactions")

      val userName = config.getUserList()(0).name

      loginForm.loginAs(userName)
    }
  }
}