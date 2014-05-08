package org.ludwiggj.finance

import org.scalatest.{Matchers, FunSuite}
import com.gargoylesoftware.htmlunit.BrowserVersion
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions._

class LoginFormIntegrationTest extends FunSuite with Matchers {

  test("login form works with third party library") {

    val config = ConfigFactory.load("cofunds.conf")
    val accounts = config.getConfigList("site.accounts") map (Account(_))
    val account: Account = accounts(0)

    val loginForm = LoginForm(WebClient(BrowserVersion.FIREFOX_24), config, "transactions")
    val loggedInPage = loginForm.login(account)

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