package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.file.PersistableToFile
import org.joda.time.LocalDate

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
  import scala.util.{Failure, Success, Try}

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
}