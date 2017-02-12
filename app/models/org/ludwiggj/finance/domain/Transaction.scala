package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.database.Tables.{Funds, FundsRow, Prices, PricesRow, Transactions, TransactionsRow, Users, UsersRow}
import models.org.ludwiggj.finance.persistence.database._
import models.org.ludwiggj.finance.persistence.file.PersistableToFile
import User.stringToUsersRow
import play.api.Play.current
import play.api.db.DB
import scala.collection.immutable.ListMap
import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._
import scala.util.{Failure, Success, Try}
import java.sql.Date

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

  def db = Database.forDataSource(DB.getDataSource("finance"))

  def insert(transaction: Transaction) {

    db.withSession {
      implicit session =>

        if (!get().contains(transaction)) {

          val userId = User.getOrInsert(transaction.userName)

          def insert(fundId: Long) {
            Transactions += TransactionsRow(
              fundId, userId, transaction.date, transaction.description, transaction.in, transaction.out,
              transaction.priceDate, transaction.units
            )
          }

          Price.insert(transaction.price)

          Fund.get(transaction.fundName) match {
            case Some(fundsRow) => insert(fundsRow.id)
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

  implicit class TransactionExtension(q: Query[Transactions, TransactionsRow, Seq]) {
    def withFunds = {
      q.join(Funds).on((t, f) => t.fundId === f.id)
    }

    def withFundsAndPricesAndUser = {
      q.join(Prices).on((t, p) => t.fundId === p.fundId && t.priceDate === p.date)
        .join(Funds).on((h_p, f) => h_p._1.fundId === f.id)
        .join(Users).on((h_p_f, u) => h_p_f._1._1.userId === u.id)
    }
  }

  implicit def bigDecimalToOption(value: BigDecimal) = {
    if (value == 0) None else Some(value)
  }

  implicit def optionToBigDecimal(value: Option[BigDecimal]): BigDecimal = {
    if (value.isDefined) value.get else 0
  }

  implicit def asListOfTransactions(q: Query[(((Transactions, Prices), Funds), Users),
    (((TransactionsRow, PricesRow), FundsRow), UsersRow), Seq]): List[Transaction] = {
    db.withSession {
      implicit session =>
        q.list map {
          case (((TransactionsRow(_, _, date, description, amountIn, amountOut, priceDate, units),
          PricesRow(_, _, price)), FundsRow(_, fundName)), UsersRow(_, userName)) =>
            Transaction(userName, date, description, amountIn, amountOut, Price(fundName, priceDate, price), units)
        }
    }
  }

  def get(): List[Transaction] = {
    db.withSession {
      implicit session =>
        Transactions.withFundsAndPricesAndUser
    }
  }

  def getRegularInvestmentDates(): List[Date] = {
    db.withSession {
      implicit session =>
        Transactions
          .filter { tx => tx.description === InvestmentRegular }
          .map {
            _.date
          }.sorted.list.distinct.reverse
    }
  }

  def getTransactionsDatesSince(dateOfInterest: Date): List[Date] = {
    db.withSession {
      implicit session =>
        Transactions
          .map { _.date }
          .filter { _ > dateOfInterest }
          .sorted.list.distinct.reverse
    }
  }

  private def getTransactionsUpToAndIncluding(dateOfInterest: Date, transactionsOfInterest: Date => TransactionsOfInterestType): TransactionMap = {
    val transactions = transactionsOfInterest(dateOfInterest)

    val sortedTransactions = ListMap(transactions.toSeq.sortBy(k => k._1): _*)

    def amountOption(amount: BigDecimal) = if (amount == 0.0) None else Some(amount)

    sortedTransactions.mapValues(rows => {
      val (_, _, fundId, _, _) = rows.head
      val latestPrice = Price.latestPrices(dateOfInterest)(fundId)

      val txs = for {
        (userName, fundName, fundId, priceRow, tx) <- rows
      } yield Transaction(userName, tx.date, tx.description, amountOption(tx.amountIn),
        amountOption(tx.amountOut), Price(fundName, priceRow.date, priceRow.price), tx.units)
      (txs, latestPrice)
    }
    )
  }

  def getTransactionsUpToAndIncluding(dateOfInterest: Date): TransactionMap = {
    def transactionsOfInterest(dateOfInterest: Date): TransactionsOfInterestType = {
      db.withSession {
        implicit session =>
          (for {
            t <- Transactions.filter {
              _.date <= dateOfInterest
            }
            f <- Funds if f.id === t.fundId
            u <- Users if u.id === t.userId
            p <- Prices if t.fundId === p.fundId && t.priceDate === p.date
          } yield (u.name, f.name, f.id, p, t)).run.groupBy(t => (t._1, t._2))
      }
    }

    getTransactionsUpToAndIncluding(dateOfInterest, transactionsOfInterest _)
  }
}