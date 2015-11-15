package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain._
import FundsDatabase.fundNameToFundsRow
import TransactionsDatabase.{InvestmentRegular, SaleForRegularPayment}
import UsersDatabase.stringToUsersRow
import org.specs2.execute.AsResult
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Around
import org.specs2.specification.mutable.SpecificationFeatures
import play.api.Play.current
import play.api.db.DB
import play.api.test.FakeApplication
import play.api.test.Helpers._

import scala.io.Source
import scala.slick.driver.MySQLDriver.simple._

trait DatabaseHelpers {
  this: SpecificationFeatures =>

  // Users
  var fatherTedUserId = 0L
  val fatherTedUserName = "Father_Ted"
  val userNameGraeme = "Graeme"
  val userNameAudrey = "Audrey"

  // Funds
  val solyentGreenFundName: FundName = "Solyent Green"
  var capitalistsDreamFundId = 0L
  val capitalistsDreamFundName: FundName = "Capitalists Dream"
  val kappaFundName: FundName = "Kappa"
  val nikeFundName: FundName = "Nike"

  // Prices
  val kappaPriceDate: FinanceDate = "20/05/2014"
  val kappaPriceInPounds: Double = 1.12
  val kappaPrice = Price(kappaFundName, kappaPriceDate, kappaPriceInPounds)

  val kappaPriceLater = Price(kappaFundName, "23/05/2014", 1.65)

  val nikePriceDateGraeme: FinanceDate = "20/06/2014"
  val nikePriceInPoundsGraeme: Double = 3.12
  val nikePriceGraeme = Price(nikeFundName, nikePriceDateGraeme, nikePriceInPoundsGraeme)

  private val nikePriceDateGraemeLater: FinanceDate = "21/06/2014"
  private val nikePriceInPoundsGraemeLater: Double = 3.08
  val nikePriceGraemeLater = Price(nikeFundName, nikePriceDateGraemeLater, nikePriceInPoundsGraemeLater)

  private val nikePriceDateGraemeLatest = "25/06/2014"
  private val nikePriceInPoundsGraemeLatest = 3.24
  val nikePriceGraemeLatest = Price(nikeFundName, nikePriceDateGraemeLatest, nikePriceInPoundsGraemeLatest)

  private val nikePriceDateAudrey: FinanceDate = "22/06/2014"
  private val nikePriceInPoundsAudrey: Double = 3.01
  val nikePriceAudrey = Price(nikeFundName, nikePriceDateAudrey, nikePriceInPoundsAudrey)

  private val nikePriceDateAudreyLater: FinanceDate = "27/06/2014"
  private val nikePriceInPoundsAudreyLater: Double = 3.29
  private val nikePriceAudreyLater = Price(nikeFundName, nikePriceDateAudreyLater, nikePriceInPoundsAudreyLater)

  // Transactions
  val kappaTransactionGraeme = Transaction(userNameGraeme, kappaPriceDate, InvestmentRegular, Some(282.1), None,
    kappaPrice, 251.875)

  val nikeTransactionGraeme = Transaction(userNameGraeme, nikePriceDateGraeme, InvestmentRegular, Some(3.5), None,
    nikePriceGraeme, 1.122)

  val nikeTransactionGraemeLater = Transaction(userNameGraeme, nikePriceDateGraemeLater, SaleForRegularPayment, None,
    Some(2.0), nikePriceGraemeLater, 0.649)

  private val nikeTransactionGraemeLatest = Transaction(userNameGraeme, nikePriceDateGraemeLatest, InvestmentRegular,
    Some(10.2), None, nikePriceGraemeLatest, 3.322)

  val nikeTransactionAudrey = Transaction(userNameAudrey, nikePriceDateAudrey, InvestmentRegular, Some(9.12),
    None, nikePriceAudrey, 3.03)

  private val nikeTransactionAudreyLater = Transaction(userNameAudrey, nikePriceDateAudreyLater, InvestmentRegular,
    Some(10.2), None, nikePriceAudreyLater, 3.1)

  // Holdings
  val kappaFundHolding = Holding(userNameGraeme, kappaPrice, 1.23)
  val nikeFundHolding = Holding(userNameGraeme, nikePriceGraeme, 1.89)

  trait Schema extends Around {

    def sqlFiles = List("1.sql", "2.sql", "3.sql", "4.sql", "5.sql")

    def ddls = for {
      sqlFile <- sqlFiles
      evolutionContent = Source.fromFile(s"conf/evolutions/finance/$sqlFile").getLines.mkString("\n")
      splitEvolutionContent = evolutionContent.split("# --- !Ups")
      upsDowns = splitEvolutionContent(1).split("# --- !Downs")
    } yield (upsDowns(1), upsDowns(0))

    def dropDdls = (ddls map {
      _._1
    }).reverse

    def createDdls = ddls map {
      _._2
    }

    def dropCreateDb() = {
      DB.withConnection("finance") { implicit connection =>

        for (ddl <- dropDdls ++ createDdls) {
          connection.createStatement.execute(ddl)
        }
      }
    }

    def around[T: AsResult](test: => T) = {

      def getConfig = Map(
        "db.finance.driver" -> "com.mysql.jdbc.Driver",
        "db.finance.url" -> "jdbc:mysql://localhost:3306/financeTest",
        "db.finance.user" -> "financeTest",
        "db.finance.password" -> "geckoTest",
        "db.finance.maxConnectionAge" -> 0,
        "db.finance.disableConnectionTracking" -> true
      )

      def fakeApp[T](block: => T): T = {
        val fakeApplication = FakeApplication(additionalConfiguration = getConfig)

        running(fakeApplication) {
          def db = Database.forDataSource(DB.getDataSource("finance")(fakeApplication))
          db.withSession { implicit s: Session => block }
        }
      }

      fakeApp {
        dropCreateDb()
        test.asInstanceOf[MatchResult[T]].toResult
      }
    }
  }

  object EmptySchema extends Schema {
  }

  object SingleUser extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      fatherTedUserId = UsersDatabase().insert(fatherTedUserName)
      test
    }
  }

  object SingleFund extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      capitalistsDreamFundId = FundsDatabase().insert(capitalistsDreamFundName)
      test
    }
  }

  object SinglePrice extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      PricesDatabase().insert(kappaPrice)
      test
    }
  }

  object TwoPrices extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      PricesDatabase().insert(List(kappaPrice, nikePriceGraeme))
      test
    }
  }

  object MultiplePricesForTwoFunds extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      PricesDatabase().insert(List(
        kappaPrice, kappaPriceLater, nikePriceGraeme, nikePriceGraemeLater, nikePriceGraemeLatest)
      )

      test
    }
  }

  object TwoHoldings extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      HoldingsDatabase().insert(List(kappaFundHolding, nikeFundHolding))
      test
    }
  }

  object SingleTransaction extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      TransactionsDatabase().insert(nikeTransactionGraeme)
      test
    }
  }

  object MultipleTransactionsForTwoUsersAndTwoFunds extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      TransactionsDatabase().insert(List(
        kappaTransactionGraeme,
        nikeTransactionGraeme, nikeTransactionGraemeLater, nikeTransactionGraemeLatest,
        nikeTransactionAudrey, nikeTransactionAudreyLater
      )
      )
      test
    }
  }

  object RegularInvestmentTransactions extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      val database = TransactionsDatabase()

      val nikeFundTx140620 =
        Transaction(userNameGraeme, "20/06/2014", InvestmentRegular, Some(2.0), None, nikePriceGraeme, 1.234)

      val nikeFundTx140520 =
        Transaction(userNameGraeme, "20/05/2014", InvestmentRegular, Some(2.0), None, nikePriceGraeme, 1.34)

      val nikeFundTx150520 =
        Transaction(userNameGraeme, "20/05/2015", InvestmentRegular, Some(2.0), None, nikePriceGraeme, 1.64)

      val secondNikeFundTx140520 =
        Transaction(userNameGraeme, "20/05/2014", InvestmentRegular, Some(5.0), None, nikePriceGraeme, 1.34)

      database.insert(nikeFundTx140620)
      database.insert(nikeFundTx140520)
      database.insert(nikeFundTx150520)
      database.insert(secondNikeFundTx140520)

      test
    }
  }
}