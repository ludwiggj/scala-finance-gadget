package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.file.PersistableToFile
import org.joda.time.LocalDate

abstract case class Transaction(userName: String,
                                date: LocalDate,
                                category: TransactionCategory,
                                in: Option[BigDecimal],
                                out: Option[BigDecimal],
                                price: Price,
                                units: BigDecimal) extends PersistableToFile {

  val fundName: FundName = price.fundName

  val priceDate: LocalDate = price.date

  val priceInPounds: BigDecimal = price.inPounds

  override def toString: String =
    s"Tx [userName: $userName, holding: ${price.fundName}, date: ${FormattableLocalDate(date)}, " +
      s"category: $category, in: $in, out: $out, price date: ${FormattableLocalDate(price.date)}, " +
      s"price: ${price.inPounds}, units: $units]"

  def toFileFormat: String = s"${price.fundName}$separator${FormattableLocalDate(date)}$separator$category" +
    s"$separator${in.getOrElse("")}$separator${out.getOrElse("")}" +
    s"$separator${FormattableLocalDate(price.date)}$separator${price.inPounds}$separator$units"

  def canEqual(t: Transaction): Boolean = (fundName == t.fundName) && (userName == t.userName) && (date == t.date) &&
    (category == t.category) && (in == t.in) && (out == t.out) && (priceDate == t.priceDate) &&
    (units == t.units)

  // TODO - equals just excludes price in pounds - check this is correct...
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
    result = prime * result + (if (category == null) 0 else category.hashCode)
    result = prime * result + (if (in.isEmpty) 0 else in.hashCode)
    result = prime * result + (if (out.isEmpty) 0 else out.hashCode)
    result = prime * result + (if (priceDate == null) 0 else priceDate.hashCode)
    result = prime * result + units.intValue()
    result
  }

  // Must define copy method manually
  def copy(userName: String = this.userName,
           date: LocalDate = this.date,
           category: TransactionCategory = this.category,
           in: Option[BigDecimal] = this.in,
           out: Option[BigDecimal] = this.out,
           price: Price = this.price,
           units: BigDecimal = this.units): Transaction = {
    Transaction(userName, date, category, in, out, price, units)
  }

  def shouldBePersistedIntoDb: Boolean = category.shouldBePersistedIntoDb && fundName != FundName("CII Cash")

  def isCashIn: Boolean = category.isCashIn

  def isUnitsIn: Boolean = in.isDefined || (category == UnitShareConversionIn)

  def isUnitsOut: Boolean = out.isDefined || (category == UnitShareConversionOut)
}

object Transaction {

  import TransactionCategory.aTransactionCategory
  import models.org.ludwiggj.finance.aLocalDate

  import scala.util.{Failure, Success, Try}

  val numberOfDecimalPlaces = 4

  private def aBigDecimalOption(candidateNumber: String): Option[BigDecimal] = {
    val decimal = Try(aBigDecimal(candidateNumber))

    decimal match {
      case Success(value) => Some(value)
      case Failure(_) => None
    }
  }

  def apply(userName: String, row: Array[String]): Transaction = {
    val fundName = FundName(row(0))
    val date = aLocalDate(row(1))
    val category = aTransactionCategory(row(2))
    val in = aBigDecimalOption(row(3))
    val out = aBigDecimalOption(row(4))
    val priceDate = aLocalDate(row(5))
    val priceInPounds = aBigDecimal(row(6))
    val units = aBigDecimal(row(7))

    Transaction(userName, date, category, in, out, Price(fundName, priceDate, priceInPounds), units)
  }

  def apply(userName: String, date: LocalDate, category: TransactionCategory, in: Option[BigDecimal],
            out: Option[BigDecimal], price: Price, units: BigDecimal): Transaction = {
    new Transaction(userName,
      date,
      category,
      in.map(scaled(_, numberOfDecimalPlaces)),
      out.map(scaled(_, numberOfDecimalPlaces)),
      price,
      scaled(units, numberOfDecimalPlaces)
    ) {}
  }
}