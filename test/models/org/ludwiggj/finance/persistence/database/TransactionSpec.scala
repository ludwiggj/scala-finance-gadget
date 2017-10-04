package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.aLocalDate
import models.org.ludwiggj.finance.domain.{FundName, InvestmentRegular, Transaction}
import models.org.ludwiggj.finance.persistence.database.Fixtures._
import models.org.ludwiggj.finance.persistence.database.PKs.PK
import org.joda.time.LocalDate
import org.scalatest.{BeforeAndAfter, Inside}
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

class TransactionSpec extends PlaySpec with OneAppPerSuite with HasDatabaseConfigProvider[JdbcProfile] with BeforeAndAfter with Inside {

  lazy val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]

  before {
    val dbAPI = app.injector.instanceOf[DBApi]
    val defaultDatabase = dbAPI.database("default")
    Evolutions.cleanupEvolutions(defaultDatabase)
    Evolutions.applyEvolutions(defaultDatabase)
  }

  val databaseLayer = new DatabaseLayer(app.injector.instanceOf[DatabaseConfigProvider].get)

  import databaseLayer._

  object SingleTransaction {
    val txUserANike140620 = txUserA("nike140620")

    def insert() = {
      exec(Transactions.insert(txUserANike140620))
    }
  }

  object RegularInvestmentTransactions {
    val priceNike150520 = price("nike150520")
    val priceNike140620 = price("nike140620")
    val priceNike140520 = price("nike140520")

    def insert() = {
      exec(Transactions.insert(List(
        Transaction(userA, priceNike140620.date, InvestmentRegular, Some(2.0), None, priceNike140620, 1.234),
        Transaction(userA, priceNike140520.date, InvestmentRegular, Some(2.0), None, priceNike140520, 1.34),
        Transaction(userA, priceNike150520.date, InvestmentRegular, Some(2.0), None, priceNike150520, 1.64),
        Transaction(userA, priceNike140520.date, InvestmentRegular, Some(5.0), None, priceNike140520, 1.34)
      )))
    }
  }

  object MultipleTransactionsForSingleUser {
    private val txUserANike140621 = txUserA("nike140621")
    val priceNike140621 = txUserANike140621.price

    private val txUserANike140625 = txUserA("nike140625")
    val priceNike140625 = txUserANike140625.price

    def insert() = {
      exec(Transactions.insert(List(
        txUserA("nike140620"),
        txUserANike140621,
        txUserANike140621,
        txUserANike140625
      )))
    }
  }

  object MultipleTransactionsForTwoUsersAndTwoFunds {
    val txUserANike140620 = txUserA("nike140620")

    val txUserANike140621 = txUserA("nike140621")
    val priceNike140621 = txUserANike140621.price

    private val txUserANike140625 = txUserA("nike140625")
    val priceNike140625 = txUserANike140625.price

    val txUserAKappa140520 = txUserA("kappa140520")
    val priceKappa140520 = txUserAKappa140520.price

    val txUserBNike140622 = txUserB("nike140622")
    val priceNike140622 = txUserBNike140622.price

    def insert() = {
      exec(Transactions.insert(List(
        txUserAKappa140520,
        txUserANike140620,
        txUserANike140621,
        txUserANike140625,
        txUserBNike140622,
        txUserB("nike140627")
      )))
    }
  }

  private def get(tx: Transaction) = {
    exec(Transactions.get(tx.fundName, tx.userName, tx.date, tx.description, tx.in, tx.out, tx.priceDate, tx.units))
  }

  private def verifyCanGetTransaction(txId: PK[databaseLayer.TransactionTable], tx: Transaction): Unit = {
    inside(get(tx).get) {
      case TransactionRow(id, _, _, date, description, amountIn, amountOut, priceDate, units) =>
        (id, date, description, amountIn, amountOut, priceDate, units) must equal(
          (txId, tx.date, tx.description, tx.in, tx.out, tx.priceDate, tx.units)
        )
    }
  }

  "get a single transaction" should {
    "return empty if transaction is not present" in {
      get(txUserA("kappa140520")) must equal(None)
    }

    "return existing transaction if it is present" in {
      import SingleTransaction._
      val txId = SingleTransaction.insert()

      verifyCanGetTransaction(txId, txUserANike140620)
    }

    "be unchanged if attempt to add transaction for same date" in {
      import SingleTransaction._
      val txId = SingleTransaction.insert()

      exec(Transactions.insert(txUserANike140620.copy(in = Some(1.34))))

      verifyCanGetTransaction(txId, txUserANike140620)
    }
  }

  "insert transaction" should {
    "insert user, fund and price if they are not present" in {

      val kappaTx = txUserA("kappa140520")
      val kappaFundName = kappaTx.fundName
      val kappaPrice = kappaTx.price
      val kappaPriceDate = kappaPrice.date
      val kappaUserName = kappaTx.userName

      exec(Users.get(kappaUserName)) mustBe None
      exec(Funds.get(kappaFundName)) mustBe None
      exec(Prices.get(kappaFundName, kappaPriceDate)) mustBe None

      val txId = exec(Transactions.insert(kappaTx))

      inside(exec(Users.get(kappaUserName)).get) {
        case UserRow(_, name, _) => name must equal(userA)
      }

      inside(exec(Funds.get(kappaFundName)).get) {
        case FundRow(_, name) => name must equal(kappaFundName)
      }

      inside(exec(Prices.get(kappaFundName, kappaPriceDate)).get) {
        case PriceRow(_, _, date, amount) => (date, amount) must equal(
          (kappaPrice.date, kappaPrice.inPounds)
        )
      }

      verifyCanGetTransaction(txId, kappaTx)
    }
  }

  "get regular investment dates" should {
    "return unique investment dates in order with most recent date first" in {
      RegularInvestmentTransactions.insert()

      import RegularInvestmentTransactions._

      val expectedDates: List[LocalDate] = List(priceNike150520.date, priceNike140620.date, priceNike140520.date)

      exec(Transactions.getRegularInvestmentDates()) must contain theSameElementsInOrderAs expectedDates
    }
  }

  "get investment dates since date" should {
    "return all transaction dates after specified date in order with most recent date first" in {
      MultipleTransactionsForSingleUser.insert()
      import MultipleTransactionsForSingleUser._

      val expectedDates: List[LocalDate] = List(priceNike140625.date, priceNike140621.date)

      exec(Transactions.getDatesSince(aLocalDate("20/06/2014"))) must contain theSameElementsInOrderAs expectedDates
    }

    "return all transaction dates after specified date for specified user in order with most recent date first" in {
      MultipleTransactionsForTwoUsersAndTwoFunds.insert()
      import MultipleTransactionsForTwoUsersAndTwoFunds._

      val expectedDates: List[LocalDate] = List(priceNike140625.date, priceNike140621.date)

      exec(Transactions.getDatesSince(aLocalDate("20/06/2014"), userA)) must contain theSameElementsInOrderAs expectedDates
    }
  }

  "get transactions up to and including date" should {
    "return all transactions up to and including date for both users" in {
      MultipleTransactionsForTwoUsersAndTwoFunds.insert()
      import MultipleTransactionsForTwoUsersAndTwoFunds._

      val transactionMap: TransactionsPerUserAndFund = exec(Transactions.getTransactionsUntil(aLocalDate("22/06/2014")))

      transactionMap must contain(
        (userA, FundName("Kappa")) -> (Seq(txUserAKappa140520), priceKappa140520)
      )

      transactionMap must contain(
        (userB, FundName("Nike")) -> (Seq(txUserBNike140622), priceNike140622)
      )

      transactionMap must contain(
        (userA, FundName("Nike")) -> (Seq(txUserANike140620, txUserANike140621), priceNike140622)
      )

      transactionMap.size must equal(3)
    }

    "omit more transactions for an earlier date" in {
      MultipleTransactionsForTwoUsersAndTwoFunds.insert()
      import MultipleTransactionsForTwoUsersAndTwoFunds._

      val transactionMap: TransactionsPerUserAndFund = exec(Transactions.getTransactionsUntil(aLocalDate("20/06/2014")))

      transactionMap must contain(
        (userA, FundName("Kappa")) -> (Seq(txUserAKappa140520), priceKappa140520)
      )

      transactionMap must contain(
        (userA, FundName("Nike")) -> (Seq(txUserANike140620), priceNike140621)
      )

      transactionMap.size must equal(2)
    }

    "return all transactions up to and including date for a specified user" in {
      MultipleTransactionsForTwoUsersAndTwoFunds.insert()
      import MultipleTransactionsForTwoUsersAndTwoFunds._

      val transactionMap: TransactionsPerUserAndFund =
        exec(Transactions.getTransactionsUntil(aLocalDate("22/06/2014"), userB))

      transactionMap must contain(
        (userB, FundName("Nike")) -> (Seq(txUserBNike140622), priceNike140622)
      )

      transactionMap.size must equal(1)
    }
  }
}