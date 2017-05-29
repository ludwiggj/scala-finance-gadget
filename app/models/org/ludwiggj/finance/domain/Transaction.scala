package models.org.ludwiggj.finance.domain

import java.sql.Date

import models.org.ludwiggj.finance.domain.User.stringToUsersRow
import models.org.ludwiggj.finance.persistence.database.Tables._
import models.org.ludwiggj.finance.persistence.database._
import models.org.ludwiggj.finance.persistence.file.PersistableToFile
import play.api.Play.current
import play.api.db.DB
import scala.collection.immutable.ListMap
import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._
import scala.util.{Failure, Success, Try}

case class Transaction(val userName: String, val date: FinanceDate, val description: String, val in: Option[BigDecimal],
                       val out: Option[BigDecimal], val price: Price, val units: BigDecimal) extends PersistableToFile {

  val fundName = price.fundName

  val priceDate = price.date

  val priceInPounds = price.inPounds

  override def toString =
    s"Tx [userName: ${userName}, holding: ${price.fundName}, date: $date, description: $description, in: $in, out: $out, " +
      s"price date: ${price.date}, price: ${price.inPounds}, units: $units]"

  def toFileFormat = s"${price.fundName}$separator$date$separator$description" +
    s"$separator${in.getOrElse("")}$separator${out.getOrElse("")}" +
    s"$separator${price.date}$separator${price.inPounds}$separator$units"

  def canEqual(t: Transaction) = (fundName == t.fundName) && (userName == t.userName) && (date == t.date) &&
    (description == t.description) && (in == t.in) && (out == t.out) && (priceDate == t.priceDate) &&
    (units == t.units)

  override def equals(that: Any): Boolean =
    that match {
      case that: Transaction => that.canEqual(this) && (this.hashCode == that.hashCode)
      case _ => false
    }

  override def hashCode: Int = {
    val prime = 31
    var result = 1
    result = prime * result + (if (userName == null) 0 else userName.hashCode)
    result = prime * result + (if (fundName == null) 0 else fundName.hashCode)
    result = prime * result + (if (date == null) 0 else date.hashCode)
    result = prime * result + (if (description == null) 0 else description.hashCode)
    result = prime * result + (if (!in.isDefined) 0 else in.hashCode)
    result = prime * result + (if (!out.isDefined) 0 else out.hashCode)
    result = prime * result + (if (priceDate == null) 0 else priceDate.hashCode)
    result = prime * result + units.intValue();
    result
  }
}

object Transaction {

  // TODO - Make these an enum (or equivalent)
  // Something along these lines...
  /*
  sealed abstract class TransactionTypeBase
  case class TransactionType(value: String)
  object InvestmentRegular extends TransactionType("Investment Regular")
   */

  val InvestmentRegular = "Investment Regular"
  val InvestmentLumpSum = "Investment Lump Sum"
  val DividendReinvestment = "Dividend Reinvestment"
  val SaleForRegularPayment = "Sale for Regular Payment"
  val UnitShareConversionIn = "Unit/Share Conversion +"
  val UnitShareConversionOut = "Unit/Share Conversion -"

  private implicit def parseNumberOption(candidateNumber: String): Option[BigDecimal] = {
    val filteredNumber = stripNonFPDigits(candidateNumber)
    if (filteredNumber.size == 0) None else Some(stringToBigDecimal(filteredNumber))
  }

  def apply(userName: String, row: String): Transaction = {
    val txPattern = (
      """.*?<td[^>]*>(.*?)</td>""" +
        """.*?<td[^>]*>(.*?)</td>.*?""" +
        """.*?<td[^>]*>(.*?)</td>.*?""" +
        """.*?<span.*?>([^<]+)</span>.*?""" +
        """.*?<span.*?>([^<]+)</span>.*?""" +
        """.*?<td[^>]*>(.*?)</td>.*?""" +
        """.*?<td[^>]*>(.*?)</td>.*?""" +
        """.*?<td[^>]*>(.*?)</td>.*?""" +
        """.*?<td[^>]*>(.*?)</td>""" +
        """.*?"""
      ).r

    val txPattern(fundName, date, description, in, out, priceDate, priceInPence, units, _) =
      stripAllWhitespaceExceptSpace(row)

    import models.org.ludwiggj.finance.domain.stringToBigDecimal

    val priceInPounds = Try(stringToBigDecimal(priceInPence) / 100) match {
      case Success(price) => price
      case Failure(ex: NumberFormatException) => BigDecimal(0)
      case Failure(ex) => throw ex
    }

    Transaction(userName, date, description.trim, in, out, Price(fundName, priceDate, priceInPounds), units)
  }

  def apply(userName: String, row: Array[String]): Transaction = {
    Transaction(userName, row(1), row(2), row(3), row(4), Price(row(0), row(5), row(6)), row(7))
  }

  // Database interactions
  def db = Database.forDataSource(DB.getDataSource("finance"))

  // TODO - The following 2 implicits shouldn't be needed...
  implicit def bigDecimalToOption(value: BigDecimal) = {
    if (value == 0) None else Some(value)
  }

  implicit def optionToBigDecimal(value: Option[BigDecimal]): BigDecimal = {
    if (value.isDefined) value.get else 0
  }

  def insert(transaction: Transaction) {

    db.withSession {
      implicit session =>

        if (!get().contains(transaction)) {
          def insert(fundId: Long, userId: Long) {
            Transactions += TransactionsRow(
              fundId, userId, transaction.date, transaction.description, transaction.in, transaction.out,
              transaction.priceDate, transaction.units
            )
          }

          val userId = User.getOrInsert(transaction.userName)
          Price.insert(transaction.price)

          Fund.get(transaction.fundName) match {
            case Some(fundsRow) => insert(fundsRow.id, userId)
            case _ => println(s"Could not insert Transaction: fund ${transaction.fundName} not found")
          }
        }
    }
  }

  def insert(transactions: List[Transaction]): Unit = {
    for (transaction <- transactions) {
      insert(transaction)
    }
  }

  def get(): List[Transaction] = {
    val txQuery = Transactions
      .join(Prices).on((t, p) => t.fundId === p.fundId && t.priceDate === p.date)
      .join(Funds).on((h_p, f) => h_p._1.fundId === f.id)
      .join(Users).on((h_p_f, u) => h_p_f._1._1.userId === u.id)

    db.withSession {
      implicit session =>
        txQuery.list map {
          case
            (
              (
                (
                  TransactionsRow(_, _, date, description, amountIn, amountOut, priceDate, units),
                  PricesRow(_, _, price)
                  ),
                FundsRow(_, fundName)
                ),
              UsersRow(_, userName, _)
              )
          =>
            Transaction(userName, date, description, amountIn, amountOut, Price(fundName, priceDate, price), units)
        }
    }
  }

  private def getDates(transactionFilter: (Transactions) => Column[Boolean]): List[Date] = {
    db.withSession {
      implicit session =>
        Transactions
          .filter {
            transactionFilter(_)
          }
          .map {
            _.date
          }.sorted.list.distinct.reverse
    }
  }

  def getRegularInvestmentDates(): List[Date] = {
    def transactionFilter(t: Transactions) = {
      t.description === InvestmentRegular
    }

    getDates(transactionFilter _)
  }

  def getDatesSince(dateOfInterest: Date): List[Date] = {
    def transactionFilter(t: Transactions) = {
      t.date > dateOfInterest
    }

    getDates(transactionFilter _)
  }

  def getDatesSince(dateOfInterest: Date, userName: String): List[Date] = {
    db.withSession {
      implicit session =>
        Transactions.innerJoin(Users).on(_.userId === _.id)
          .filter { case (_, u) => u.name === userName }
          .map { case (t, _) => t.date }
          .filter {
            _ > dateOfInterest
          }
          .sorted.list.distinct.reverse
    }
  }

  private def getTransactionsUntil(dateOfInterest: Date,
                                   userFilter: (Transactions, Users) => Column[Boolean]): TransactionsPerUserAndFund = {

    def amountOption(amount: BigDecimal) = if (amount == 0.0) None else Some(amount)

    val candidates = db.withSession {
      implicit session =>
        (for {
          t <- Transactions.filter {
            _.date <= dateOfInterest
          }
          f <- Funds if f.id === t.fundId
          u <- Users if userFilter(t, u)
          p <- Prices if t.fundId === p.fundId && t.priceDate === p.date
        } yield (u.name, f.name, f.id, p, t)).run.groupBy(t => (t._1, t._2))
    }

    val sortedCandidates = ListMap(candidates.toSeq.sortBy(k => k._1): _*)

    sortedCandidates.mapValues(rows => {
      val fundId = rows.head._3
      val latestPrice = Price.latestPrices(dateOfInterest)(fundId)

      val txs = for {
        (userName, fundName, fundId, priceRow, tx) <- rows
      } yield
        Transaction(
          userName, tx.date, tx.description, amountOption(tx.amountIn),
          amountOption(tx.amountOut), Price(fundName, priceRow.date, priceRow.price), tx.units
        )

      (txs, latestPrice)
    }
    )
  }

  def getTransactionsUntil(dateOfInterest: Date): TransactionsPerUserAndFund = {
    def userFilter(t: Transactions, u: Users) = {
      u.id === t.userId
    }

    getTransactionsUntil(dateOfInterest, userFilter _)
  }

  def getTransactionsUntil(dateOfInterest: Date, userName: String): TransactionsPerUserAndFund = {
    def userFilter(t: Transactions, u: Users) = {
      u.id === t.userId && u.name === userName
    }

    getTransactionsUntil(dateOfInterest, userFilter _)
  }
}