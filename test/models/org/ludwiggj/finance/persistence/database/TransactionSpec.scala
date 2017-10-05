package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.{FundName, Transaction}
import models.org.ludwiggj.finance.persistence.database.fixtures._
import models.org.ludwiggj.finance.aLocalDate
import org.joda.time.LocalDate
import org.scalatest.{BeforeAndAfter, Inside}
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

class TransactionSpec extends PlaySpec with OneAppPerSuite with HasDatabaseConfigProvider[JdbcProfile] with BeforeAndAfter with Inside {

  lazy val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]

  before {
    val dbAPI = app.injector.instanceOf[DBApi]
    val defaultDatabase = dbAPI.database("default")
    Evolutions.cleanupEvolutions(defaultDatabase)
    Evolutions.applyEvolutions(defaultDatabase)
  }

  private val config: DatabaseConfig[JdbcProfile] = app.injector.instanceOf[DatabaseConfigProvider].get

  val databaseLayer = new DatabaseLayer(config)

  import databaseLayer._

  private def get(tx: Transaction) = {
    exec(Transactions.get(tx.fundName, tx.userName, tx.date, tx.description, tx.in, tx.out, tx.priceDate, tx.units))
  }

  "the Transaction API" should {
    "provide a get method," which {
      "returns empty if the transaction is not present" in new DatabaseLayer(config) {
        get(txUserA("kappa140520")) must equal(None)
      }

      // TODO matchPattern is the idiomatic way to do this match, but it fails on txId for some unknown reason
      // TODO Should be able to generalise this code, as it appears several times, but am being defeated by type mismatches
      "returns the existing transaction if it is present" in new DatabaseLayer(config) with SingleTransaction {
        inside(get(txUserANike140620).get) {
          case databaseLayer.TransactionRow(id, _, _, date, description, amountIn, amountOut, priceDate, units) =>
            (id, date, description, amountIn, amountOut, priceDate, units) must equal(
              (
                txId, txUserANike140620.date, txUserANike140620.description, txUserANike140620.in, txUserANike140620.out,
                txUserANike140620.priceDate, txUserANike140620.units
              )
            )
        }
      }

      "returns the existing transaction after an attempt to add transaction for the same date" in
        new DatabaseLayer(config) with SingleTransaction {
          exec(Transactions.insert(txUserANike140620.copy(in = Some(1.34))))

          inside(get(txUserANike140620).get) {
            case databaseLayer.TransactionRow(id, _, _, date, description, amountIn, amountOut, priceDate, units) =>
              (id, date, description, amountIn, amountOut, priceDate, units) must equal(
                (
                  txId, txUserANike140620.date, txUserANike140620.description, txUserANike140620.in, txUserANike140620.out,
                  txUserANike140620.priceDate, txUserANike140620.units
                )
              )
          }
        }
    }

    "provide an insert method," which {
      "inserts the user, fund and price if they are not present" in new DatabaseLayer(config) {

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

        inside(get(kappaTx).get) {
          case databaseLayer.TransactionRow(id, _, _, date, description, amountIn, amountOut, priceDate, units) =>
            (id, date, description, amountIn, amountOut, priceDate, units) must equal(
              (
                txId, kappaTx.date, kappaTx.description, kappaTx.in, kappaTx.out,
                kappaTx.priceDate, kappaTx.units
              )
            )
        }
      }
    }

    "provide a getRegularInvestmentDates method," which {
      "returns the unique investment dates in order with most recent date first" in
        new DatabaseLayer(config) with RegularInvestmentTransactions {

          val expectedDates: List[LocalDate] = List(priceNike150520.date, priceNike140620.date, priceNike140520.date)

          exec(Transactions.getRegularInvestmentDates()) must contain theSameElementsInOrderAs expectedDates
        }
    }

    "provide a getDatesSince method," which {
      "returns all transaction dates after specified date in order with most recent date first" in
        new DatabaseLayer(config) with MultipleTransactionsForSingleUser {
          val expectedDates: List[LocalDate] = List(priceNike140625.date, priceNike140621.date)

          exec(Transactions.getDatesSince(aLocalDate("20/06/2014"))) must contain theSameElementsInOrderAs expectedDates
        }

      "returns all transaction dates after specified date for specified user in order with most recent date first" in
        new DatabaseLayer(config) with MultipleTransactionsForTwoUsersAndTwoFunds {

          val expectedDates: List[LocalDate] = List(priceNike140625.date, priceNike140621.date)

          exec(Transactions.getDatesSince(aLocalDate("20/06/2014"), userA)) must contain theSameElementsInOrderAs expectedDates
        }
    }

    "provide a getTransactionsUntil method," which {
      "returns all transactions up to and including date for both users" in
        new DatabaseLayer(config) with MultipleTransactionsForTwoUsersAndTwoFunds {

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

      "omits more transactions for an earlier date" in {
        new DatabaseLayer(config) with MultipleTransactionsForTwoUsersAndTwoFunds {

          val transactionMap: TransactionsPerUserAndFund = exec(Transactions.getTransactionsUntil(aLocalDate("20/06/2014")))

          transactionMap must contain(
            (userA, FundName("Kappa")) -> (Seq(txUserAKappa140520), priceKappa140520)
          )

          transactionMap must contain(
            (userA, FundName("Nike")) -> (Seq(txUserANike140620), priceNike140621)
          )

          transactionMap.size must equal(2)
        }
      }

      "returns all transactions up to and including date for a specified user" in
        new DatabaseLayer(config) with MultipleTransactionsForTwoUsersAndTwoFunds {

          val transactionMap: TransactionsPerUserAndFund =
            exec(Transactions.getTransactionsUntil(aLocalDate("22/06/2014"), userB))

          transactionMap must contain(
            (userB, FundName("Nike")) -> (Seq(txUserBNike140622), priceNike140622)
          )

          transactionMap.size must equal(1)
        }
    }
  }
}