package org.ludwiggj.finance.persistence.database

import java.sql.Date
import models.org.ludwiggj.finance.domain.Transaction
import models.org.ludwiggj.finance.persistence.database.Tables.{UsersRow, FundsRow}
import models.org.ludwiggj.finance.persistence.database._
import models.org.ludwiggj.finance.stringToSqlDate
import org.specs2.matcher.MapMatchers
import org.specs2.mutable.Specification

class TransactionsSpec extends Specification with DatabaseHelpers with MapMatchers {

  "insert transaction" should {
    "insert user, fund and price if they are not present" in EmptySchema {
      val fundsDatabase = FundsDatabase()
      val pricesDatabase = PricesDatabase()
      val usersDatabase = UsersDatabase()
      val transactionsDatabase = TransactionsDatabase()

      val userName = "Graeme"
      val kappaFundTransaction = Transaction(userNameGraeme,
        kappaPriceDate, "A transaction", Some(2.0), None, kappaPrice, 1.234)

      usersDatabase.get(userName) must beNone
      fundsDatabase.get(kappaFundName) must beNone
      pricesDatabase.get(kappaFundName, kappaPriceDate) must beNone

      transactionsDatabase.insert(kappaFundTransaction)

      println(TransactionsDatabase().get())

      usersDatabase.get(userName) must beSome.which(
        _ match { case UsersRow(_, name) => name == userName })

      fundsDatabase.get(kappaFundName) must beSome.which(
        _ match { case FundsRow(_, name) => name == kappaFundName.name })

      pricesDatabase.get(kappaFundName, kappaPriceDate) must beSome(kappaPrice)

      transactionsDatabase.get() must containTheSameElementsAs(List(kappaFundTransaction))
    }
  }

  "get a list of transactions" should {
    "be unchanged if attempt to add duplicate transaction" in SingleTransaction {
      val transactionsDatabase = TransactionsDatabase()

      transactionsDatabase.get().size must beEqualTo(1)

      transactionsDatabase.insert(nikeTransactionGraeme)

      transactionsDatabase.get().size must beEqualTo(1)
    }
  }

  "get a list of transactions" should {
    "increase by one if add same transaction for different user" in SingleTransaction {
      val transactionsDatabase = TransactionsDatabase()

      transactionsDatabase.get().size must beEqualTo(1)

      val duplicateTransactionForAnotherUser =
        Transaction(userNameAudrey, nikePriceDateGraeme, "A transaction", Some(2.0), None, nikePriceGraeme, 1.234)

      transactionsDatabase.insert(duplicateTransactionForAnotherUser)

      transactionsDatabase.get().size must beEqualTo(2)
    }
  }

  "get regular investment dates" should {
    "return unique investment dates in order with most recent date first" in RegularInvestmentTransactions {
      val transactionsDatabase = TransactionsDatabase()

      val expectedDates: List[Date] = List(
        Date.valueOf("2015-05-20"),
        Date.valueOf("2014-06-20"),
        Date.valueOf("2014-05-20")
      )

      // test
      TransactionsDatabase().getTransactionsUpToAndIncluding("21/6/2014")

      transactionsDatabase.getRegularInvestmentDates() must containTheSameElementsAs(expectedDates)
    }
  }

  "get transactions up to and including date" should {
    "return all transactions up to and including date for both users" in MultipleTransactionsForTwoUsersAndTwoFunds {

      val transactionMap: TransactionMap = TransactionsDatabase().getTransactionsUpToAndIncluding("22/6/2014")

      transactionMap must havePairs(
        (userNameGraeme, kappaFundName: String) -> (Seq(kappaTransactionGraeme), kappaPrice),
        (userNameAudrey, nikeFundName: String) -> (Seq(nikeTransactionAudrey), nikePriceAudrey),
        (userNameGraeme, nikeFundName: String) -> (Seq(nikeTransactionGraeme, nikeTransactionGraemeLater), nikePriceAudrey)
      )

      transactionMap.size must beEqualTo(3)
    }
  }

  "get transactions up to and including date" should {
    "omit more transactions for an earlier date" in MultipleTransactionsForTwoUsersAndTwoFunds {

      val transactionMap: TransactionMap = TransactionsDatabase().getTransactionsUpToAndIncluding("20/6/2014")

      transactionMap must havePairs(
        (userNameGraeme, kappaFundName: String) -> (Seq(kappaTransactionGraeme), kappaPrice),
        (userNameGraeme, nikeFundName: String) -> (Seq(nikeTransactionGraeme), nikePriceGraemeLater)
      )

      transactionMap.size must beEqualTo(2)
    }
  }
}