package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.{Fund, Transaction, User, _}
import models.org.ludwiggj.finance.domain.Fund.fundNameToFundsRow
import models.org.ludwiggj.finance.domain.Transaction._
import models.org.ludwiggj.finance.persistence.database.Tables.UsersRow

// TODO - additional private vals?

trait DatabaseHelpers {
  // Prices
  val kappaPrice140512 = Price("Kappa", "12/05/2014", 2.50)
  val kappaPriceZero140516 = Price("Kappa", "16/05/2014", 0.0)
  val kappaPrice140520, kappaPrice = Price("Kappa", "20/05/2014", 1.12)
  val kappaPrice140523 = Price("Kappa", "23/05/2014", 1.65)

  val kappaIIPrice140524 = Price("Kappa II", "24/05/2014", 1.66)

  val nikePrice140620, nikePrice = Price("Nike", "20/06/2014", 3.12)
  val nikePrice140621 = Price("Nike", "21/06/2014", 3.08)
  val nikePrice140622 = Price("Nike", "22/06/2014", 3.01)
  val nikePrice140625 = Price("Nike", "25/06/2014", 3.24)
  val nikePrice140627 = Price("Nike", "27/06/2014", 3.29)

  // Transactions
  val userAKappaTx140520 =
    Transaction("User A", kappaPrice.date, InvestmentRegular, Some(282.1), None, kappaPrice, 251.875)

  val userANikeTx140620 =
    Transaction("User A", nikePrice140620.date, InvestmentRegular, Some(3.5), None, nikePrice140620, 1.122)

  val userANikeTx140621 =
    Transaction("User A", nikePrice140621.date, SaleForRegularPayment, None, Some(2.0), nikePrice140621, 0.649)

  val userANikeTx140625 =
    Transaction("User A", nikePrice140625.date, InvestmentRegular, Some(10.2), None, nikePrice140625, 3.322)

  val userBNikeTx140622 =
    Transaction("User B", nikePrice140622.date, InvestmentRegular, Some(9.12), None, nikePrice140622, 3.03)

  val userBNikeTx140627 =
    Transaction("User B", nikePrice140627.date, InvestmentRegular, Some(10.2), None, nikePrice140627, 3.1)

  trait Schema {
    def loadData(): Unit = {
    }
  }

  object EmptySchema extends Schema {
  }

  object SingleUser extends Schema {
    var userId = 0L
    val ted = UsersRow(0L, "Father_Ted", Some("Penitent_Man"))
    override def loadData() = {
      userId = User.insert(ted)
    }
  }

  object SingleFund extends Schema {
    val fundName: FundName = "Capitalists Dream"
    var fundId = 0L
    override def loadData() = {
      fundId = Fund.insert(fundName)
    }
  }

  object SinglePrice extends Schema {
    override def loadData() = {
      Price.insert(kappaPrice)
    }
  }

  object TwoPrices extends Schema {
    override def loadData() = {
      Price.insert(List(kappaPrice, nikePrice))
    }
  }

  object MultiplePricesForSingleFund extends Schema {
    override def loadData() = {
      Price.insert(List(kappaPrice140512, kappaPriceZero140516, kappaPrice140520, kappaPrice140523))
    }
  }

  object MultiplePricesForSingleFundAndItsRenamedEquivalent extends Schema {
    override def loadData() = {
      Price.insert(List(kappaPrice140512, kappaPriceZero140516, kappaPrice140520, kappaPrice140523, kappaIIPrice140524))
    }
  }

  object MultiplePricesForTwoFunds extends Schema {
    override def loadData() = {
      Price.insert(List(kappaPrice140520, kappaPrice140523, nikePrice140620, nikePrice140621, nikePrice140625))
    }
  }

  object SingleTransaction extends Schema {
    override def loadData() = {
      Transaction.insert(userANikeTx140620)
    }
  }

  object MultipleTransactionsForTwoUsersAndTwoFunds extends Schema {
    override def loadData() = {
      Transaction.insert(List(
        userAKappaTx140520,
        userANikeTx140620, userANikeTx140621, userANikeTx140625,
        userBNikeTx140622, userBNikeTx140627
      )
      )
    }
  }

  object MultipleTransactionsForSingleUser extends Schema {
    override def loadData() = {
      Transaction.insert(List(userANikeTx140620, userANikeTx140621, userANikeTx140621, userANikeTx140625))
    }
  }

  object RegularInvestmentTransactions extends Schema {
    override def loadData() = {
      val nikePrice140520 = Price("Nike", "20/05/2014", 2.97)
      val nikePrice150520 = Price("Nike", "20/05/2015", 3.28)

      val nikeFundTx140620 =
        Transaction("User A", nikePrice140620.date, InvestmentRegular, Some(2.0), None, nikePrice140620, 1.234)

      val nikeFundTx140520 =
        Transaction("User A", nikePrice140520.date, InvestmentRegular, Some(2.0), None, nikePrice140520, 1.34)

      val nikeFundTx150520 =
        Transaction("User A", nikePrice150520.date, InvestmentRegular, Some(2.0), None, nikePrice150520, 1.64)

      val secondNikeFundTx140520 =
        Transaction("User A", nikePrice140520.date, InvestmentRegular, Some(5.0), None, nikePrice140520, 1.34)

      Transaction.insert(List(nikeFundTx140620, nikeFundTx140520, nikeFundTx150520, secondNikeFundTx140520))
    }
  }

}