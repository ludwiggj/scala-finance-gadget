package org.ludwiggj.finance.examples

import scala.language.postfixOps
import org.ludwiggj.finance.builders.LoginFormBuilder._
import org.ludwiggj.finance.web.{WebSiteConfig, WebSiteHoldingFactory}
import org.ludwiggj.finance.persistence.Persister
import com.github.nscala_time.time.Imports._

object FinanceValuationScraper extends App {
  val config = WebSiteConfig("cofunds.conf")
  val loginFormBuilder = aLoginForm().basedOnConfig(config)

  val accounts = config.getAccountList()

  val date = DateTime.now.toString(DateTimeFormat.forPattern("yyyy_MM_dd"))

  for (account <- accounts) {
    val accountName = account.name
    val holdingFactory = WebSiteHoldingFactory(loginFormBuilder, accountName)

    val holdings = holdingFactory.getHoldings()
    println(s"Total holdings ($accountName): £${holdings map (h => h.value) sum}")
    val persister = Persister(s"resources/${accountName}_${date}.txt")

    persister.write(holdings)
  }
}