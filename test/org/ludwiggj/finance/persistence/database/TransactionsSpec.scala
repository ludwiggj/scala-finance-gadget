package org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.Transaction
import models.org.ludwiggj.finance.persistence.database.Tables.{UsersRow, FundsRow}
import models.org.ludwiggj.finance.persistence.database._
import org.specs2.mutable.Specification

class TransactionsSpec extends Specification with DatabaseHelpers {

  "insert transaction" should {
    "insert user, fund and price if they are not present" in EmptySchema {
      val fundsDatabase = FundsDatabase()
      val pricesDatabase = PricesDatabase()
      val usersDatabase = UsersDatabase()
      val transactionsDatabase = TransactionsDatabase()

      val userName = "Graeme"
      val kappaFundTransaction = Transaction(
        kappaFundPriceDate, "A transaction", Some(2.0), None, kappaFundPrice, 1.234)

      usersDatabase.get(userName) must beNone
      fundsDatabase.get(kappaFundName) must beNone
      pricesDatabase.get(kappaFundName, kappaFundPriceDate) must beNone

      transactionsDatabase.insert(userName, kappaFundTransaction)

      println(TransactionsDatabase().get())

      usersDatabase.get(userName) must beSome.which(
        _ match { case UsersRow(_, name) => name == userName })

      fundsDatabase.get(kappaFundName) must beSome.which(
        _ match { case FundsRow(_, name) => name == kappaFundName })

      pricesDatabase.get(kappaFundName, kappaFundPriceDate) must beSome(kappaFundPrice)

      transactionsDatabase.get() must containTheSameElementsAs(List(kappaFundTransaction))
    }
  }

  "get a list of transactions" should {
    "be unchanged if attempt to add duplicate transaction" in SingleTransaction {
      val transactionsDatabase = TransactionsDatabase()

      transactionsDatabase.get().size must beEqualTo(1)

      transactionsDatabase.insert(userName, nikeFundTransaction)

      transactionsDatabase.get().size must beEqualTo(1)
    }
  }
}