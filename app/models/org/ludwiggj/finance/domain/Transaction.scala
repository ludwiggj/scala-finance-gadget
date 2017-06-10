package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.database.PKs.PK
import models.org.ludwiggj.finance.persistence.database.Tables._
import models.org.ludwiggj.finance.persistence.database._
import models.org.ludwiggj.finance.persistence.file.PersistableToFile
import org.joda.time.LocalDate
import play.api.Play.current
import play.api.db.DB
import scala.collection.immutable.ListMap
import scala.slick.driver.MySQLDriver.simple._
import scala.util.{Failure, Success, Try}

case class Transaction(userName: String,
                       date: LocalDate,
                       description: TransactionType,
                       in: Option[BigDecimal],
                       out: Option[BigDecimal],
                       price: Price,
                       units: BigDecimal) extends PersistableToFile {

  val fundName = price.fundName

  val priceDate = price.date

  val priceInPounds = price.inPounds

  override def toString =
    s"Tx [userName: ${userName}, holding: ${price.fundName}, date: ${FormattableLocalDate(date)}, description: $description, in: $in, out: $out, " +
      s"price date: ${FormattableLocalDate(price.date)}, price: ${price.inPounds}, units: $units]"

  def toFileFormat = s"${price.fundName}$separator${FormattableLocalDate(date)}$separator$description" +
    s"$separator${in.getOrElse("")}$separator${out.getOrElse("")}" +
    s"$separator${FormattableLocalDate(price.date)}$separator${price.inPounds}$separator$units"

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

  import models.org.ludwiggj.finance.aLocalDate

  import TransactionType.aTransactionType

  private def aBigDecimalOption(candidateNumber: String): Option[BigDecimal] = {
    val decimal = Try(aBigDecimal(candidateNumber))

    decimal match {
      case Success(value) => Some(value)
      case Failure(ex) => None
    }
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

    val priceInPounds = Try(aBigDecimal(priceInPence) / 100) match {
      case Success(price) => s"$price"
      case Failure(ex: NumberFormatException) => "0"
      case Failure(ex) => throw ex
    }

    Transaction(userName, Array(fundName, date, description.trim, in, out, priceDate, priceInPounds, units))
  }

  def apply(userName: String, row: Array[String]): Transaction = {
    val fundName = FundName(row(0))
    val date = aLocalDate(row(1))
    val description = aTransactionType(row(2))
    val in = aBigDecimalOption(row(3))
    val out = aBigDecimalOption(row(4))
    val priceDate = aLocalDate(row(5))
    val priceInPounds = aBigDecimal(row(6))
    val units = aBigDecimal(row(7))

    Transaction(userName, date, description, in, out, Price(fundName, priceDate, priceInPounds), units)
  }

  // Database interactions
  def db = Database.forDataSource(DB.getDataSource("finance"))

  def insert(transaction: Transaction) {

    db.withSession {
      implicit session =>

        if (!get().contains(transaction)) {
          def insert(fundId: PK[FundTable], userId: PK[UserTable]) {
            Transactions += TransactionRow(
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
                  TransactionRow(_, _, date, description, amountIn, amountOut, priceDate, units),
                  PriceRow(_, _, price)
                  ),
                FundRow(_, fundName)
                ),
              UserRow(_, userName, _)
              )
          =>
            Transaction(userName, date, description, amountIn, amountOut, Price(fundName, priceDate, price), units)
        }
    }
  }

  private def getDates(transactionFilter: (TransactionTable) => Column[Boolean]): List[LocalDate] = {
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

  def getRegularInvestmentDates(): List[LocalDate] = {
    def transactionFilter(t: TransactionTable) = {
      t.description === (InvestmentRegular: TransactionType)
    }

    getDates(transactionFilter _)
  }

  def getDatesSince(dateOfInterest: LocalDate): List[LocalDate] = {
    def transactionFilter(t: TransactionTable) = {
      t.date > dateOfInterest
    }

    getDates(transactionFilter _)
  }

  def getDatesSince(dateOfInterest: LocalDate, userName: String): List[LocalDate] = {
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

  private def getTransactionsUntil(dateOfInterest: LocalDate, userFilter: (TransactionTable, UserTable)
    => Column[Boolean]): TransactionsPerUserAndFund = {

    def getTransactionCandidates(): TransactionCandidates = {
      db.withSession {
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
    }

    // Following import used to avoid 'diverging implicit expansion for type scala.math.Ordering' error
    // as per https://issues.scala-lang.org/browse/SI-8541
    import Ordering.Tuple2

    val txCandidates = getTransactionCandidates.toSeq.sortBy(userNameFundName => userNameFundName._1)

    val sortedTxCandidates = ListMap(txCandidates: _*)

    sortedTxCandidates.mapValues(rows => {
      val fundId = rows.head._3
      val latestPrice = Price.latestPrices(dateOfInterest)(fundId)

      val txs = for {
        (userName, fundName, fundId, priceRow, tx) <- rows
      } yield
        Transaction(
          userName, tx.date, tx.description, tx.amountIn, tx.amountOut,
          Price(fundName, priceRow.date, priceRow.price), tx.units
        )

      (txs, latestPrice)
    }
    )
  }

  def getTransactionsUntil(dateOfInterest: LocalDate): TransactionsPerUserAndFund = {
    def userFilter(t: TransactionTable, u: UserTable) = {
      u.id === t.userId
    }

    getTransactionsUntil(dateOfInterest, userFilter _)
  }

  def getTransactionsUntil(dateOfInterest: LocalDate, userName: String): TransactionsPerUserAndFund = {
    def userFilter(t: TransactionTable, u: UserTable) = {
      u.id === t.userId && u.name === userName
    }

    getTransactionsUntil(dateOfInterest, userFilter _)
  }
}