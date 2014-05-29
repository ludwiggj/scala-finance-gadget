package org.ludwiggj.finance.web

import org.scalatest.{Matchers, FunSuite}
import org.scalamock.scalatest.MockFactory
import org.ludwiggj.finance.builders.LoginFormBuilder
import scala.io.Source.fromURL
import org.ludwiggj.finance.domain.{Transaction, FinanceDate}

class WebSiteTransactionFactoryTest extends FunSuite with MockFactory with Matchers {

  test("Retrieve transactions from web page") {
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
    }

    val tx1 = Transaction(
    "Aberdeen Ethical World Equity A Fund Inc", FinanceDate("02/05/2014"), "Dividend Reinvestment",
    Some(BigDecimal(0.27)), None, FinanceDate("02/05/2014"), BigDecimal(141.23), BigDecimal(0.1912)
    )

    val tx2 = Transaction(
    "Ecclesiastical Amity European A Fund Inc", FinanceDate("02/05/2014"), "Dividend Reinvestment",
    Some(BigDecimal(5.06)), None, FinanceDate("02/05/2014"), BigDecimal(202.90), BigDecimal(2.4939)
    )

    val tx3 = Transaction(
    "F&C Stewardship Income 1 Fund Inc", FinanceDate("02/05/2014"), "Dividend Reinvestment",
    Some(BigDecimal(17.39)), None, FinanceDate("02/05/2014"), BigDecimal(132.50), BigDecimal(13.1246)
    )

    val tx4 = Transaction(
    "M&G Feeder of Property Portfolio I Fund Acc", FinanceDate("25/04/2014"), "Investment Regular",
    Some(BigDecimal(200.00)), None, FinanceDate("25/04/2014"), BigDecimal(1153.08), BigDecimal(17.3449)
    )

    val expectedTransactions = List(tx1, tx2, tx3, tx4)

    val webSiteTransactionFactory =
      WebSiteTransactionFactory(loginFormBuilder, accountName)

    val transactions = webSiteTransactionFactory.getTransactions()

    transactions should contain theSameElementsAs expectedTransactions
  }
}