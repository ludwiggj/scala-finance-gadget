package org.ludwiggj.finance.application

import com.gargoylesoftware.htmlunit.ElementNotFoundException
import scala.language.postfixOps
import org.ludwiggj.finance.builders.LoginFormBuilder._
import org.ludwiggj.finance.web.{NotAuthenticatedException, WebSiteConfig, WebSiteTransactionFactory}
import org.ludwiggj.finance.persistence.database.DatabasePersister
import org.ludwiggj.finance.persistence.file.FilePersister
import com.github.nscala_time.time.Imports._

object FinanceTransactionScraper extends App {
  val config = WebSiteConfig("cofunds.conf")
  val loginFormBuilder = aLoginForm().basedOnConfig(config)

  val accounts = config.getAccountList()

  val date = DateTime.now.toString(DateTimeFormat.forPattern("yy_MM_dd"))

  for (account <- accounts) {
    val accountName = account.name
    time(s"processTransactions($accountName)",
      try {
        val transactionFactory = WebSiteTransactionFactory(loginFormBuilder, accountName)
        val transactions = transactionFactory.getTransactions()

        println(s"Total transactions ($accountName): ${transactions size}")

        val persister = FilePersister(s"$reportHome/txs_${date}_${accountName}.txt")

        persister.write(transactions)
        new DatabasePersister().persistTransactions(accountName, transactions)
      } catch {
        case ex: ElementNotFoundException =>
          println(s"Cannot retrieve transactions for $accountName, ${ex.toString}")
        case ex: NotAuthenticatedException =>
          println(s"Cannot retrieve transactions for $accountName [NotAuthenticatedException]")
      })
  }
}