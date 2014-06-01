package org.ludwiggj.finance.examples

import scala.language.postfixOps
import org.ludwiggj.finance.builders.LoginFormBuilder._
import org.ludwiggj.finance.web.{WebSiteConfig, WebSiteHoldingFactory}
import org.ludwiggj.finance.persistence.Persister

object FinanceValuationScraper extends App {
  val config = WebSiteConfig("cofunds.conf")
  val loginFormBuilder = aLoginForm().basedOnConfig(config)

  val accounts = config.getAccountList()

  for (account <- accounts) {
    val accountName = account.name
    val holdingFactory = WebSiteHoldingFactory(loginFormBuilder, accountName)
    val holdings = holdingFactory.getHoldings()

    println(s"Total holdings ($accountName): £${holdings map (h => h.value) sum}")

    val persister = Persister(s"resources/$accountName")

    persister.write(holdings)
  }
}