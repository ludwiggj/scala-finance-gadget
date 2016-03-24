package models.org.ludwiggj.finance.persistence.database

import java.sql.Date
import models.org.ludwiggj.finance.Transaction
import models.org.ludwiggj.finance.Transaction._
import models.org.ludwiggj.finance.stringToSqlDate
import Tables.{FundsRow, UsersRow}
import org.scalatest.{BeforeAndAfter, DoNotDiscover, Inside}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}

@DoNotDiscover
class TransactionsSpec extends PlaySpec with DatabaseHelpers with ConfiguredApp with BeforeAndAfter with Inside {

  before {
    DatabaseCleaner.recreateDb()
  }

  "insert transaction" should {
    "insert user, fund and price if they are not present" in {
      EmptySchema.loadData()

      val fundsDatabase = FundsDatabase()
      val pricesDatabase = PricesDatabase()
      val usersDatabase = UsersDatabase()
      val userName = "Graeme"
      val kappaFundTransaction = Transaction(userNameGraeme,
        kappaPriceDate, InvestmentRegular, Some(2.0), None, kappaPrice, 1.234)

      usersDatabase.get(userName) mustBe None
      fundsDatabase.get(kappaFundName) mustBe None
      pricesDatabase.get(kappaFundName, kappaPriceDate) mustBe None

      Transaction.insert(kappaFundTransaction)

      inside(usersDatabase.get(userName).get) { case UsersRow(_, name) =>
        name must equal(userName)
      }

      inside(fundsDatabase.get(kappaFundName).get) { case FundsRow(_, name) =>
        name must equal(kappaFundName.name)
      }

      pricesDatabase.get(kappaFundName, kappaPriceDate) mustBe Some(kappaPrice)

      Transaction.get() must contain theSameElementsAs List(kappaFundTransaction)
    }
  }

  "get a list of transactions" should {
    "be unchanged if attempt to add duplicate transaction" in {
      SingleTransaction.loadData()

      Transaction.get().size must equal(1)

      Transaction.insert(nikeTransactionGraeme)

      Transaction.get().size must equal(1)
    }

    "increase by one if add same transaction for different user" in {
      SingleTransaction.loadData()

      Transaction.get().size must equal(1)

      val duplicateTransactionForAnotherUser =
        Transaction(userNameAudrey, nikePriceDateGraeme, InvestmentRegular, Some(2.0), None, nikePriceGraeme, 1.234)

      Transaction.insert(duplicateTransactionForAnotherUser)

      Transaction.get().size must equal(2)
    }
  }

  "get regular investment dates" should {
    "return unique investment dates in order with most recent date first" in {
      RegularInvestmentTransactions.loadData()

      val expectedDates: List[Date] = List(
        Date.valueOf("2015-05-20"),
        Date.valueOf("2014-06-20"),
        Date.valueOf("2014-05-20")
      )

      Transaction.getRegularInvestmentDates() must contain theSameElementsAs expectedDates
    }
  }

  "get investment dates since date" should {
    "return all transaction dates since specified date in order with most recent date first" in {
      MultipleTransactionsForSingleUser.loadData()

      val expectedDates: List[Date] = List(
        Date.valueOf("2014-06-25"),
        Date.valueOf("2014-06-21")
      )

      Transaction.getTransactionsDatesSince("20/6/2014") must contain theSameElementsAs expectedDates
    }
  }

  "get transactions up to and including date" should {
    "return all transactions up to and including date for both users" in {
      MultipleTransactionsForTwoUsersAndTwoFunds.loadData()

      val transactionMap: TransactionMap = Transaction.getTransactionsUpToAndIncluding("22/6/2014")

      transactionMap must contain (
        (userNameGraeme, kappaFundName: String) -> (Seq(kappaTransactionGraeme), kappaPrice)
      )

      transactionMap must contain (
        (userNameAudrey, nikeFundName: String) -> (Seq(nikeTransactionAudrey), nikePriceAudrey)
      )

      transactionMap must contain (
        (userNameGraeme, nikeFundName: String) -> (Seq(nikeTransactionGraeme, nikeTransactionGraemeLater), nikePriceAudrey)
      )

      transactionMap.size must equal(3)
    }

    "omit more transactions for an earlier date" in {
      MultipleTransactionsForTwoUsersAndTwoFunds.loadData()

      val transactionMap: TransactionMap = Transaction.getTransactionsUpToAndIncluding("20/6/2014")

      transactionMap must contain (
        (userNameGraeme, kappaFundName: String) -> (Seq(kappaTransactionGraeme), kappaPrice)
      )

      transactionMap must contain (
        (userNameGraeme, nikeFundName: String) -> (Seq(nikeTransactionGraeme), nikePriceGraemeLater)
      )

      transactionMap.size must equal(2)
    }
  }
}