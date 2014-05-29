package org.ludwiggj.finance.web

import org.scalatest.{Matchers, FunSuite}
import org.scalamock.scalatest.MockFactory
import org.ludwiggj.finance.builders.LoginFormBuilder
import scala.io.Source.fromURL
import org.ludwiggj.finance.domain.{Holding, FinanceDate}

class WebSiteHoldingFactoryTest extends FunSuite with MockFactory with Matchers {

  test("Retrieve holdings from web page") {
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
    }

    val holding1 = Holding("Aberdeen Ethical World Equity A Fund Inc", BigDecimal(1887.9336),
      FinanceDate("20/05/2014"), BigDecimal(143.60))

    val holding2 = Holding("Henderson Global Care UK Income A Fund Inc", BigDecimal(3564.2985),
      FinanceDate("20/05/2014"), BigDecimal(116.20))

    val holding3 = Holding("Schroder Gbl Property Income Maximiser Z Fund Inc", BigDecimal(5498.5076),
      FinanceDate("20/05/2014"), BigDecimal(48.08))

    val expectedHoldings = List(holding1, holding2, holding3)

    val webSiteHoldingFactory =
      WebSiteHoldingFactory(loginFormBuilder, accountName)

    val holdings = webSiteHoldingFactory.getHoldings()

    println(holdings)

    holdings should contain theSameElementsAs expectedHoldings
  }
}