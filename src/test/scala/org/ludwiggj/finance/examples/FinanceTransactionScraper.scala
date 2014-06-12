package org.ludwiggj.finance.examples

import scala.language.postfixOps
import org.ludwiggj.finance.builders.LoginFormBuilder._
import org.ludwiggj.finance.web.{WebSiteTransactionFactory, WebSiteConfig}
import org.ludwiggj.finance.persistence.Persister
import com.github.nscala_time.time.Imports._

object FinanceTransactionScraper extends App {
  val config = WebSiteConfig("cofunds.conf")
  val loginFormBuilder = aLoginForm().basedOnConfig(config)

  val accounts = config.getAccountList()

  val date = DateTime.now.toString(DateTimeFormat.forPattern("yy_MM_dd"))

  for (account <- accounts) {
    val accountName = account.name
    val transactionFactory = WebSiteTransactionFactory(loginFormBuilder, accountName)
    val transactions = transactionFactory.getTransactions()

    println(s"Total transactions ($accountName): ${transactions size}")

    val persister = Persister(s"resources/txs_${date}_${accountName}.txt")

    persister.write(transactions)
  }
}