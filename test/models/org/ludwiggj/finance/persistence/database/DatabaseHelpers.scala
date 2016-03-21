package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain._
import models.org.ludwiggj.finance.persistence.database.FundsDatabase.fundNameToFundsRow
import models.org.ludwiggj.finance.persistence.database.TransactionsDatabase.{InvestmentRegular, SaleForRegularPayment}
import models.org.ludwiggj.finance.persistence.database.UsersDatabase.stringToUsersRow

trait DatabaseHelpers {
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
  val kappaIIFundName: FundName = "Kappa II"
  val nikeFundName: FundName = "Nike"

  // Prices
  val kappaPriceEarliestDate: FinanceDate = "12/05/2014"
  val kappaPriceEarliestInPounds: Double = 2.50
  val kappaPriceEarliest = Price(kappaFundName, kappaPriceEarliestDate, kappaPriceEarliestInPounds)

  val kappaPriceEarlyButZeroDate: FinanceDate = "16/05/2014"
  val kappaPriceEarlyButZeroInPounds: Double = 0.0
  val kappaPriceEarlyButZero = Price(kappaFundName, kappaPriceEarlyButZeroDate, kappaPriceEarlyButZeroInPounds)

  val kappaPriceDate: FinanceDate = "20/05/2014"
  val kappaPriceInPounds: Double = 1.12
  val kappaPrice = Price(kappaFundName, kappaPriceDate, kappaPriceInPounds)

  val kappaPriceLater = Price(kappaFundName, "23/05/2014", 1.65)

  val kappaIIPrice = Price(kappaIIFundName, "24/05/2014", 1.66)

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

  private val nikeTransactionGraemeSameDateAsLater = Transaction(userNameGraeme, nikePriceDateGraemeLater, InvestmentRegular,
    Some(10.2), None, nikePriceGraemeLatest, 3.322)

  private val nikeTransactionGraemeLatest = Transaction(userNameGraeme, nikePriceDateGraemeLatest, InvestmentRegular,
    Some(10.2), None, nikePriceGraemeLatest, 3.322)

  val nikeTransactionAudrey = Transaction(userNameAudrey, nikePriceDateAudrey, InvestmentRegular, Some(9.12),
    None, nikePriceAudrey, 3.03)

  private val nikeTransactionAudreyLater = Transaction(userNameAudrey, nikePriceDateAudreyLater, InvestmentRegular,
    Some(10.2), None, nikePriceAudreyLater, 3.1)

  // Holdings
  val kappaFundHolding = Holding(userNameGraeme, kappaPrice, 1.23)
  val nikeFundHolding = Holding(userNameGraeme, nikePriceGraeme, 1.89)

  trait Schema {
    def loadData(): Unit = {
    }
  }

  object EmptySchema extends Schema {
  }

  object SingleUser extends Schema {
    override def loadData() = {
      fatherTedUserId = UsersDatabase().insert(fatherTedUserName)
    }
  }

  object SingleFund extends Schema {
    override def loadData() = {
      capitalistsDreamFundId = FundsDatabase().insert(capitalistsDreamFundName)
    }
  }

  object SinglePrice extends Schema {
    override def loadData() = {
      PricesDatabase().insert(kappaPrice)
    }
  }

  object TwoPrices extends Schema {
    override def loadData() = {
      PricesDatabase().insert(List(kappaPrice, nikePriceGraeme))
    }
  }

  object MultiplePricesForSingleFund extends Schema {
    override def loadData() = {
      PricesDatabase().insert(List(kappaPriceEarliest, kappaPriceEarlyButZero, kappaPrice, kappaPriceLater))
    }
  }

  object MultiplePricesForSingleFundAndItsRenamedEquivalent extends Schema {
    override def loadData() = {
      PricesDatabase().insert(
        List(kappaPriceEarliest, kappaPriceEarlyButZero, kappaPrice, kappaPriceLater, kappaIIPrice
        ))
    }
  }

  object MultiplePricesForTwoFunds extends Schema {
    override def loadData() = {
      PricesDatabase().insert(List(
        kappaPrice, kappaPriceLater, nikePriceGraeme, nikePriceGraemeLater, nikePriceGraemeLatest)
      )
    }
  }

  object TwoHoldings extends Schema {
    override def loadData() = {
      HoldingsDatabase().insert(List(kappaFundHolding, nikeFundHolding))
    }
  }

  object SingleTransaction extends Schema {
    override def loadData() = {
      TransactionsDatabase().insert(nikeTransactionGraeme)
    }
  }

  object MultipleTransactionsForTwoUsersAndTwoFunds extends Schema {
    override def loadData() = {
      TransactionsDatabase().insert(List(
        kappaTransactionGraeme,
        nikeTransactionGraeme, nikeTransactionGraemeLater, nikeTransactionGraemeLatest,
        nikeTransactionAudrey, nikeTransactionAudreyLater
      )
      )
    }
  }

  object MultipleTransactionsForSingleUser extends Schema {
    override def loadData() = {
      TransactionsDatabase().insert(List(
        nikeTransactionGraeme,
        nikeTransactionGraemeLater,
        nikeTransactionGraemeSameDateAsLater,
        nikeTransactionGraemeLatest
      )
      )
    }
  }

  object RegularInvestmentTransactions extends Schema {
    override def loadData() = {
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
    }
  }
}