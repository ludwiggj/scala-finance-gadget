package org.ludwiggj.finance.examples

import scala.language.postfixOps
import org.ludwiggj.finance.builders.LoginFormBuilder._
import org.ludwiggj.finance.web.{WebSiteTransactionFactory, WebSiteConfig, WebSiteHoldingFactory}

object FinanceValuationScraper extends App {
//  private val headerRow = 1

  // Retrieve from web site
  //  val page = loginAndParse("valuations")

  // Load from file...
  //  val page = parsePageFromFile("resources/summary.txt")

  //  val holdingRows = page.select(s"table[id~=Holdings] tr") drop headerRow
  //
  //  val holdings = for (holdingRow <- holdingRows) yield Holding(holdingRow.toString)

  val config = WebSiteConfig("cofunds.conf")
  val accountName = config.getAccountList()(0).name
  val loginFormBuilder = aLoginForm().basedOnConfig(config)

  val holdingFactory = WebSiteHoldingFactory(loginFormBuilder, accountName)
  val holdings = holdingFactory.getHoldings()

  println(s"${holdings.size} rows")

  holdings.foreach(h => println(h))

  println(s"Total holdings (map + sum): £${holdings map (h => h.value) sum}")

  println(s"Total holdings (fold): £${holdings.foldLeft(BigDecimal(0)) {
    (sum, h) => sum + h.value
  }}")

  val transactionFactory = WebSiteTransactionFactory(loginFormBuilder, accountName)
  val txs = transactionFactory.getTransactions()

  println(s"${txs.size} rows")
  txs.foreach(t => println(t))
  println(txs.toList.groupBy(_.description))
}