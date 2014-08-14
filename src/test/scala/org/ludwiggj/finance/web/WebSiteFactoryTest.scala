package org.ludwiggj.finance.web

import org.scalatest.{Matchers, FunSuite}
import org.scalamock.scalatest.MockFactory
import org.ludwiggj.finance.builders.LoginFormBuilder
import scala.io.Source.fromURL
import org.ludwiggj.finance.{TestTransactions, TestHoldings}

class WebSiteFactoryTest extends FunSuite with MockFactory with Matchers {

  test("Retrieve holdings from web page") {
    new TestHoldings {
      val accountName = "testAccount"
      val loginFormBuilder = mock[LoginFormBuilder]
      val loginForm = mock[Login]
      val htmlPage = mock[HtmlEntity]

      val mockedHoldingsSource = fromURL(getClass.getResource("/webHoldings.xml"))
      val mockedHoldingsXml = mockedHoldingsSource.mkString
      mockedHoldingsSource.close()

      inSequence {
        (loginFormBuilder.loggingIntoPage _).expects("valuations").returning(loginFormBuilder)
        (loginFormBuilder.build _).expects().returning(loginForm)
        (loginForm.loginAs _).expects(accountName).returning(htmlPage)
        (htmlPage.asXml _).expects().returning(mockedHoldingsXml)
        (htmlPage.logOff _).expects()
      }

      val actualHoldings = WebSiteHoldingFactory(loginFormBuilder, accountName).getHoldings()

      println(actualHoldings)

      actualHoldings should contain theSameElementsAs holdings
    }
  }

  test("Retrieve transactions from web page") {
    new TestTransactions {
      val accountName = "testAccount"
      val loginFormBuilder = mock[LoginFormBuilder]
      val loginForm = mock[Login]
      val htmlPage = mock[HtmlEntity]

      val mockedTransactionsSource = fromURL(getClass.getResource("/webTransactions.xml"))
      val mockedTransactionXml = mockedTransactionsSource.mkString
      mockedTransactionsSource.close()

      inSequence {
        (loginFormBuilder.loggingIntoPage _).expects("transactions").returning(loginFormBuilder)
        (loginFormBuilder.build _).expects().returning(loginForm)
        (loginForm.loginAs _).expects(accountName).returning(htmlPage)
        (htmlPage.asXml _).expects().returning(mockedTransactionXml)
        (htmlPage.logOff _).expects()
      }

      val actualTransactions = WebSiteTransactionFactory(loginFormBuilder, accountName).getTransactions()

      actualTransactions should contain theSameElementsAs transactions
    }
  }
}