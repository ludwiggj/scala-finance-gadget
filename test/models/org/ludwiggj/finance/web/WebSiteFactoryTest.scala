// TODO - Pick over, then remove

//package models.org.ludwiggj.finance.web
//
//import models.org.ludwiggj.finance.data.{TestHoldings, TestTransactionsMultipleFunds, userA}
//import models.org.ludwiggj.finance.domain.{Holding, Transaction}
//import org.scalamock.scalatest.MockFactory
//import org.scalatest.{FunSuite, Matchers}
//
//import scala.io.Source
//import scala.io.Source.fromURL

//class WebSiteFactoryTest extends FunSuite with MockFactory with Matchers {
//
//  test("Retrieve holdings from web page") {
//    new TestHoldings {
//      val loginForm = mock[Login]
//      val htmlPage = mock[HtmlEntity]
//
//      val mockedHoldingsSource: Source = fromURL(getClass.getResource("/webHoldings.xml"))
//      val mockedHoldingsXml: String = mockedHoldingsSource.mkString
//      mockedHoldingsSource.close()
//
//      inSequence {
//        (loginForm.loginAs _).expects(userA).returning(htmlPage)
//        (htmlPage.asXml _).expects().returning(mockedHoldingsXml)
//        (htmlPage.logOff _).expects()
//      }
//
//      val actualHoldings: List[Holding] = WebSiteHoldingFactory(loginForm, userA).getHoldings()
//
//      println(actualHoldings)
//
//      actualHoldings should contain theSameElementsAs holdingsMultipleFunds
//    }
//  }
//
//  test("Retrieve transactions from web page") {
//    new TestTransactionsMultipleFunds {
//      val loginForm = mock[Login]
//      val htmlPage = mock[HtmlEntity]
//
//      val mockedTransactionsSource: Source = fromURL(getClass.getResource("/webTransactions.xml"))
//      val mockedTransactionXml: String = mockedTransactionsSource.mkString
//      mockedTransactionsSource.close()
//
//      inSequence {
//        (loginForm.loginAs _).expects(userA).returning(htmlPage)
//        (htmlPage.asXml _).expects().returning(mockedTransactionXml)
//        (htmlPage.logOff _).expects()
//      }
//
//      val actualTransactions: List[Transaction] = WebSiteTransactionFactory(loginForm, userA).getTransactions()
//
//      println(actualTransactions)
//
//      actualTransactions should contain theSameElementsAs txsMultipleFunds
//    }
//  }
//}