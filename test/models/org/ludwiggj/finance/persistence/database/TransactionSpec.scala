package models.org.ludwiggj.finance.persistence.database

import java.sql.Date

import models.org.ludwiggj.finance.domain.{Fund, Price, Transaction, User}
import models.org.ludwiggj.finance.persistence.database.Tables.{FundsRow, UsersRow}
import models.org.ludwiggj.finance.stringToSqlDate
import org.scalatest.{BeforeAndAfter, DoNotDiscover, Inside}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}

@DoNotDiscover
class TransactionSpec extends PlaySpec with DatabaseHelpers with ConfiguredApp with BeforeAndAfter with Inside {

  before {
    Database.recreate()
  }

  private val userA = "User A"

  "insert transaction" should {
    "insert user, fund and price if they are not present" in {
      EmptySchema.loadData()

      val kappaFundName = price("kappa140520").fundName
      val kappaPriceDate = price("kappa140520").date

      User.get(userA) mustBe None
      Fund.get(kappaFundName) mustBe None
      Price.get(kappaFundName, kappaPriceDate) mustBe None

      Transaction.insert(txUserA("kappa140520"))

      inside(User.get(userA).get) {
        case UsersRow(_, name, _) => name must equal(userA)
      }

      inside(Fund.get(kappaFundName).get) {
        case FundsRow(_, name) => name must equal(kappaFundName.name)
      }

      Price.get(kappaFundName, kappaPriceDate) mustBe Some(price("kappa140520"))

      Transaction.get() must contain theSameElementsAs List(txUserA("kappa140520"))
    }
  }

  "get a list of transactions" should {
    "be unchanged if attempt to add duplicate transaction" in {
      SingleTransaction.loadData()

      Transaction.get().size must equal(1)

      Transaction.insert(txUserA("nike140620"))

      Transaction.get().size must equal(1)
    }

    "increase by one if add same transaction for different user" in {
      SingleTransaction.loadData()

      Transaction.get().size must equal(1)

      Transaction.insert(txUserA("nike140620").copy(userName = "User B"))

      Transaction.get().size must equal(2)
    }
  }

  "get regular investment dates" should {
    "return unique investment dates in order with most recent date first" in {
      RegularInvestmentTransactions.loadData()

      val expectedDates: List[Date] = List(
        price("nike150520").date,
        price("nike140620").date,
        price("nike140520").date
      )

      Transaction.getRegularInvestmentDates() must contain theSameElementsInOrderAs expectedDates
    }
  }

  "get investment dates since date" should {
    "return all transaction dates after specified date in order with most recent date first" in {
      MultipleTransactionsForSingleUser.loadData()

      val expectedDates: List[Date] = List(
        price("nike140625").date,
        price("nike140621").date
      )

      Transaction.getDatesSince("20/6/2014") must contain theSameElementsInOrderAs expectedDates
    }

    "return all transaction dates after specified date for specified user in order with most recent date first" in {
      MultipleTransactionsForTwoUsersAndTwoFunds.loadData()

      val expectedDates: List[Date] = List(
        price("nike140625").date,
        price("nike140621").date
      )

      Transaction.getDatesSince("20/6/2014", userA) must contain theSameElementsInOrderAs expectedDates
    }
  }

  "get transactions up to and including date" should {
    "return all transactions up to and including date for both users" in {
      MultipleTransactionsForTwoUsersAndTwoFunds.loadData()

      val transactionMap: TransactionsPerUserAndFund = Transaction.getTransactionsUntil("22/6/2014")

      transactionMap must contain(
        (userA, "Kappa") -> (Seq(txUserA("kappa140520")), price("kappa140520"))
      )

      transactionMap must contain(
        ("User B", "Nike") -> (Seq(txUserB("nike140622")), price("nike140622"))
      )

      transactionMap must contain(
        (userA, "Nike") -> (Seq(txUserA("nike140620"), txUserA("nike140621")), price("nike140622"))
      )

      transactionMap.size must equal(3)
    }

    "omit more transactions for an earlier date" in {
      MultipleTransactionsForTwoUsersAndTwoFunds.loadData()

      val transactionMap: TransactionsPerUserAndFund = Transaction.getTransactionsUntil("20/6/2014")

      transactionMap must contain(
        (userA, "Kappa") -> (Seq(txUserA("kappa140520")), price("kappa140520"))
      )

      transactionMap must contain(
        (userA, "Nike") -> (Seq(txUserA("nike140620")), price("nike140621"))
      )

      transactionMap.size must equal(2)
    }

    "return all transactions up to and including date for a specified user" in {
      MultipleTransactionsForTwoUsersAndTwoFunds.loadData()

      val transactionMap: TransactionsPerUserAndFund = Transaction.getTransactionsUntil("22/6/2014", "User B")

      transactionMap must contain(
        ("User B", "Nike") -> (Seq(txUserB("nike140622")), price("nike140622"))
      )

      transactionMap.size must equal(1)
    }
  }
}