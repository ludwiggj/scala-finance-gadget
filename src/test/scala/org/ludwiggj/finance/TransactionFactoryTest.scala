package org.ludwiggj.finance

import org.scalatest.{Matchers, FunSuite}
import org.scalamock.scalatest.MockFactory
import com.typesafe.config.Config
import scala.collection.JavaConverters._
import org.ludwiggj.finance.builders.LoginFormBuilder._
import org.filippodeluca.ssoup.SSoup._
import org.ludwiggj.finance.Transaction

class TransactionFactoryTest extends FunSuite with MockFactory with Matchers {

  test("Retrieve transactions from web page") {
    val config = WebSiteConfig("cofunds.conf")
    val accountName = config.getAccountList()(0).name
    val loginForm = aLoginForm().basedOnConfig(config).loggingIntoPage("transactions").build()
    val transactionFactory: WebSiteTransactionFactory = WebSiteTransactionFactory(loginForm, accountName)
    val txs = transactionFactory.getTransactions()

    println(s"${txs.size} rows")

//    val config = mock[Config]
//    val attribute1 = mock[Config]
//    val attribute2 = mock[Config]
//    val attributes = List(attribute1, attribute2).asJava
//
//    inAnyOrder {
//      (config.getString _).expects("name").returning("accountName")
//      (config.getConfigList _).expects("attributes").returning(attributes)
//      (attribute1.getString _).expects("name").returning("attr1")
//      (attribute1.getString _).expects("value").returning("value1")
//      (attribute2.getString _).expects("name").returning("attr2")
//      (attribute2.getString _).expects("value").returning("value2")
//    }
//
//    val acc = Account(config)
//
//    acc.name should equal ("accountName")
//    acc.attributeValue("attr1") should equal ("value1")
//    acc.attributeValue("attr2") should equal ("value2")

    // HERE!!!

//    val t: TransactionFactory = aTransactionFactory.
  }
}

 // Retrieve from web site
  //  val page = loginAndParse("transactions")

  // Load from file...
//    val page = parsePageFromFile("resources/transactions.txt")
//
//    val txRows = page.select(s"table[id~=dgTransactions] tr") drop headerRow
//
//    println("Number of rows = " + txRows.size)
//
//    val txs = for (txRow <- txRows) yield Transaction(txRow.toString)

  // Get rows from page
//    val txRows = page.select(s"table[id~=dgTransactions] tr") drop headerRow

//    println("Number of rows = " + txRows.size)

//    val txs = for (txRow <- txRows) yield Transaction(txRow.toString)

  // Load from file II...