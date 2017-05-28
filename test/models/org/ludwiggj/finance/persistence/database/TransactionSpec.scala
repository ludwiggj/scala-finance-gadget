package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.stringToSqlDate
import models.org.ludwiggj.finance.domain.{Fund, Price, Transaction, User}
import models.org.ludwiggj.finance.domain.Transaction._
import Tables.{FundsRow, UsersRow}
import org.scalatest.{BeforeAndAfter, DoNotDiscover, Inside}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}
import java.sql.Date

@DoNotDiscover
class TransactionSpec extends PlaySpec with DatabaseHelpers with ConfiguredApp with BeforeAndAfter with Inside {

  before {
    Database.recreate()
  }

  "insert transaction" should {
    "insert user, fund and price if they are not present" in {
      EmptySchema.loadData()

      val userA = "User A"
      val kappaFundName = kappaPrice.fundName
      val kappaPriceDate = kappaPrice.date

      val kappaTx = Transaction(userA, kappaPriceDate, InvestmentRegular, Some(2.0), None, kappaPrice, 1.234)

      User.get(userA) mustBe None
      Fund.get(kappaFundName) mustBe None
      Price.get(kappaFundName, kappaPriceDate) mustBe None

      Transaction.insert(kappaTx)

      inside(User.get(userA).get) { case UsersRow(_, name, _) =>
        name must equal(userA)
      }

      inside(Fund.get(kappaFundName).get) { case FundsRow(_, name) =>
        name must equal(kappaFundName.name)
      }

      Price.get(kappaFundName, kappaPriceDate) mustBe Some(kappaPrice)

      Transaction.get() must contain theSameElementsAs List(kappaTx)
    }
  }

  "get a list of transactions" should {
    "be unchanged if attempt to add duplicate transaction" in {
      SingleTransaction.loadData()

      Transaction.get().size must equal(1)

      Transaction.insert(userANikeTx140620)

      Transaction.get().size must equal(1)
    }

    "increase by one if add same transaction for different user" in {
      SingleTransaction.loadData()

      Transaction.get().size must equal(1)

      //TODO - fix!
      val duplicateTransactionForAnotherUser =
        Transaction("User B", nikePrice.date, InvestmentRegular, Some(2.0), None, nikePrice, 1.234)

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

      Transaction.getTransactionDatesSince("20/6/2014") must contain theSameElementsAs expectedDates
    }

    "return all transaction dates since specified date for specified user in order with most recent date first" in {
      MultipleTransactionsForTwoUsersAndTwoFunds.loadData()

      val expectedDates: List[Date] = List(
        Date.valueOf("2014-06-21"),
        Date.valueOf("2014-06-25")
      )

      Transaction.getTransactionDatesSince("20/6/2014", "User A") must contain theSameElementsAs expectedDates
    }
  }

  "get transactions up to and including date" should {
    "return all transactions up to and including date for both users" in {
      MultipleTransactionsForTwoUsersAndTwoFunds.loadData()

      val transactionMap: TransactionMap = Transaction.getTransactionsUpToAndIncluding("22/6/2014")

      transactionMap must contain(
        ("User A", "Kappa") -> (Seq(userAKappaTx140520), kappaPrice)
      )

      transactionMap must contain(
        ("User B", "Nike") -> (Seq(userBNikeTx140622), nikePrice140622)
      )

      transactionMap must contain(
        ("User A", "Nike") -> (Seq(userANikeTx140620, userANikeTx140621), nikePrice140622)
      )

      transactionMap.size must equal(3)
    }

    "omit more transactions for an earlier date" in {
      MultipleTransactionsForTwoUsersAndTwoFunds.loadData()

      val transactionMap: TransactionMap = Transaction.getTransactionsUpToAndIncluding("20/6/2014")

      transactionMap must contain(
        ("User A", "Kappa") -> (Seq(userAKappaTx140520), kappaPrice140520)
      )

      transactionMap must contain(
        ("User A", "Nike") -> (Seq(userANikeTx140620), nikePrice140621)
      )

      transactionMap.size must equal(2)
    }

    "return all transactions up to and including date for a specified user" in {
      MultipleTransactionsForTwoUsersAndTwoFunds.loadData()

      val transactionMap: TransactionMap = Transaction.getTransactionsUpToAndIncluding("22/6/2014", "User B")

      transactionMap must contain(
        ("User B", "Nike") -> (Seq(userBNikeTx140622), nikePrice140622)
      )

      transactionMap.size must equal(1)
    }
  }
}