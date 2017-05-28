package models.org.ludwiggj.finance.web

import models.org.ludwiggj.finance.builders.LoginFormBuilder
import models.org.ludwiggj.finance.data.{TestHoldings, TestTransactionsMultipleFunds, userA}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSuite, Matchers}

import scala.io.Source.fromURL

class WebSiteFactoryTest extends FunSuite with MockFactory with Matchers {

  test("Retrieve holdings from web page") {
    new TestHoldings {
      val loginFormBuilder = mock[LoginFormBuilder]
      val loginForm = mock[Login]
      val htmlPage = mock[HtmlEntity]

      val mockedHoldingsSource = fromURL(getClass.getResource("/webHoldings.xml"))
      val mockedHoldingsXml = mockedHoldingsSource.mkString
      mockedHoldingsSource.close()

      inSequence {
        (loginFormBuilder.loggingIntoPage _).expects("valuations").returning(loginFormBuilder)
        (loginFormBuilder.build _).expects().returning(loginForm)
        (loginForm.loginAs _).expects(userA).returning(htmlPage)
        (htmlPage.asXml _).expects().returning(mockedHoldingsXml)
        (htmlPage.logOff _).expects()
      }

      val actualHoldings = WebSiteHoldingFactory(loginFormBuilder, userA).getHoldings()

      println(actualHoldings)

      actualHoldings should contain theSameElementsAs holdingsMultipleFunds
    }
  }

  test("Retrieve transactions from web page") {
    new TestTransactionsMultipleFunds {
      val loginFormBuilder = mock[LoginFormBuilder]
      val loginForm = mock[Login]
      val htmlPage = mock[HtmlEntity]

      val mockedTransactionsSource = fromURL(getClass.getResource("/webTransactions.xml"))
      val mockedTransactionXml = mockedTransactionsSource.mkString
      mockedTransactionsSource.close()

      inSequence {
        (loginFormBuilder.loggingIntoPage _).expects("transactions").returning(loginFormBuilder)
        (loginFormBuilder.build _).expects().returning(loginForm)
        (loginForm.loginAs _).expects(userA).returning(htmlPage)
        (htmlPage.asXml _).expects().returning(mockedTransactionXml)
        (htmlPage.logOff _).expects()
      }

      val actualTransactions = WebSiteTransactionFactory(loginFormBuilder, userA).getTransactions()

      println(actualTransactions)

      actualTransactions should contain theSameElementsAs txsMultipleFunds
    }
  }
}