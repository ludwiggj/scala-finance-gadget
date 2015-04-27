package org.ludwiggj.finance.domain

import java.sql.Date

import org.ludwiggj.finance.persistence.Persistable

class Transaction(val holdingName: String, val date: FinanceDate, val description: String,
                  val in: Option[BigDecimal], val out: Option[BigDecimal], val priceDate: FinanceDate,
                  val priceInPounds: BigDecimal, val units: BigDecimal) extends Persistable {

  def dateAsSqlDate = date.asSqlDate
  def priceDateAsSqlDate = priceDate.asSqlDate

  override def toString =
    s"Tx [holding: $holdingName, date: $date, description: $description, in: $in, out: $out, price date: $priceDate, price: $priceInPounds, units: $units]"

  def toFileFormat = s"$holdingName$separator$date$separator$description" +
    s"$separator${in.getOrElse("")}$separator${out.getOrElse("")}" +
    s"$separator$priceDate$separator$priceInPounds$separator$units"

  final override def equals(other: Any) = {
    val that = if (other.isInstanceOf[Transaction]) other.asInstanceOf[Transaction] else null
    if (that == null) false
    else {
      val objectsEqual =
        (
          (holdingName == that.holdingName) &&
            (date == that.date) &&
            (description == that.description) &&
            (in == that.in) &&
            (out == that.out) &&
            (priceDate == that.priceDate) &&
            (priceInPounds == that.priceInPounds) &&
            (units == that.units)
          )
      objectsEqual
    }
  }

  final override def hashCode = {
    var result = 17
    result = 31 * result + holdingName.hashCode
    result = 31 * result + date.hashCode
    result = 31 * result + description.hashCode
    result = 31 * result + in.hashCode
    result = 31 * result + out.hashCode
    result = 31 * result + priceDate.hashCode
    result = 31 * result + priceInPounds.hashCode
    result = 31 * result + units.hashCode
    result
  }
}

object Transaction {
  def apply(holdingName: String, date: FinanceDate, description: String, in: Option[BigDecimal],
            out: Option[BigDecimal], priceDate: FinanceDate, priceInPounds: BigDecimal, units: BigDecimal) =
    new Transaction(holdingName, date, description, in, out, priceDate, priceInPounds, units)

  // TODO This is to enable mapping from database query to Transactions - should be a better way to do this!
  def apply(tx: (String, Date, String, BigDecimal, BigDecimal, Date, BigDecimal, BigDecimal)) = {
    val (holdingName, date, description, in, out, priceDate, priceInPounds, units) = tx
    new Transaction(holdingName, FinanceDate(date), description, Option(in), Option(out), FinanceDate(priceDate),
      priceInPounds, units)
  }

  def apply(row: String): Transaction = {
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

    val txPattern(holdingName, date, description, in, out, priceDate, priceInPence, units, _) =
      stripAllWhitespaceExceptSpace(row)

    val priceInPounds = parseNumber(priceInPence) / 100;

    Transaction(cleanHoldingName(holdingName), FinanceDate(date), description.trim,
      parseNumberOption(in), parseNumberOption(out), FinanceDate(priceDate), priceInPounds, parseNumber(units))
  }

  def apply(row: Array[String]): Transaction = {
    Transaction(row(0), FinanceDate(row(1)), row(2), parseNumberOption(row(3)), parseNumberOption(row(4)),
      FinanceDate(row(5)), parseNumber(row(6)), parseNumber(row(7)))
  }
}