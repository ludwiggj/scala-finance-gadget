package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.stringToLocalDate
import models.org.ludwiggj.finance.domain.{Fund, Transaction, User, _}
import models.org.ludwiggj.finance.domain.Fund.fundNameToFundsRow
import models.org.ludwiggj.finance.domain.TransactionType._
import models.org.ludwiggj.finance.persistence.database.Tables.UsersRow

trait DatabaseHelpers {
  // Prices
  val price: Map[String, Price] = Map(
    "kappa140512" -> Price("Kappa", "12/05/2014", 2.50),
    "kappa140516" -> Price("Kappa", "16/05/2014", 0.0),
    "kappa140520" -> Price("Kappa", "20/05/2014", 1.12),
    "kappa140523" -> Price("Kappa", "23/05/2014", 1.65),
    "kappaII140524" -> Price("Kappa II", "24/05/2014", 1.66),
    "nike140520" -> Price("Nike", "20/05/2014", 2.97),
    "nike140620" -> Price("Nike", "20/06/2014", 3.12),
    "nike140621" -> Price("Nike", "21/06/2014", 3.08),
    "nike140622" -> Price("Nike", "22/06/2014", 3.01),
    "nike140625" -> Price("Nike", "25/06/2014", 3.24),
    "nike140627" -> Price("Nike", "27/06/2014", 3.29),
    "nike150520" -> Price("Nike", "20/05/2015", 3.28)
  )

  val txUserA: Map[String, Transaction] = Map(
    "kappa140520" -> Transaction(
      "User A", price("kappa140520").date, InvestmentRegular, Some(282.1), None, price("kappa140520"), 251.875),
    "nike140620" -> Transaction(
      "User A", price("nike140620").date, InvestmentRegular, Some(3.5), None, price("nike140620"), 1.122),
    "nike140621" -> Transaction(
      "User A", price("nike140621").date, SaleForRegularPayment, None, Some(2.0), price("nike140621"), 0.649),
    "nike140625" -> Transaction(
      "User A", price("nike140625").date, InvestmentRegular, Some(10.2), None, price("nike140625"), 3.322)
  )

  val txUserB: Map[String, Transaction] = Map(
    "nike140622" -> Transaction(
      "User B", price("nike140622").date, InvestmentRegular, Some(9.12), None, price("nike140622"), 3.03),
    "nike140627" -> Transaction(
      "User B", price("nike140627").date, InvestmentRegular, Some(10.2), None, price("nike140627"), 3.1)
  )

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
      Price.insert(price("kappa140520"))
    }
  }

  object TwoPrices extends Schema {
    override def loadData() = {
      Price.insert(List(
        price("kappa140520"),
        price("nike140620")
      ))
    }
  }

  object MultiplePricesForSingleFund extends Schema {
    override def loadData() = {
      Price.insert(List(
        price("kappa140512"),
        price("kappa140516"),
        price("kappa140520"),
        price("kappa140523")
      ))
    }
  }

  object MultiplePricesForSingleFundAndItsRenamedEquivalent extends Schema {
    override def loadData() = {
      Price.insert(List(
        price("kappa140512"),
        price("kappa140516"),
        price("kappa140520"),
        price("kappa140523"),
        price("kappaII140524")
      ))
    }
  }

  object MultiplePricesForTwoFunds extends Schema {
    override def loadData() = {
      Price.insert(List(
        price("kappa140520"),
        price("kappa140523"),
        price("nike140620"),
        price("nike140621"),
        price("nike140625")
      ))
    }
  }

  object SingleTransaction extends Schema {
    override def loadData() = {
      Transaction.insert(txUserA("nike140620"))
    }
  }

  object MultipleTransactionsForSingleUser extends Schema {
    override def loadData() = {
      Transaction.insert(List(
        txUserA("nike140620"),
        txUserA("nike140621"),
        txUserA("nike140621"),
        txUserA("nike140625"))
      )
    }
  }

  object MultipleTransactionsForTwoUsersAndTwoFunds extends Schema {
    override def loadData() = {
      Transaction.insert(List(
        txUserA("kappa140520"),
        txUserA("nike140620"),
        txUserA("nike140621"),
        txUserA("nike140625"),
        txUserB("nike140622"),
        txUserB("nike140627")
      ))
    }
  }

  object RegularInvestmentTransactions extends Schema {
    override def loadData() = {
      Transaction.insert(List(
        Transaction("User A", price("nike140620").date, InvestmentRegular, Some(2.0), None, price("nike140620"), 1.234),
        Transaction("User A", price("nike140520").date, InvestmentRegular, Some(2.0), None, price("nike140520"), 1.34),
        Transaction("User A", price("nike150520").date, InvestmentRegular, Some(2.0), None, price("nike150520"), 1.64),
        Transaction("User A", price("nike140520").date, InvestmentRegular, Some(5.0), None, price("nike140520"), 1.34)
      ))
    }
  }

}