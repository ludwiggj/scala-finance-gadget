package org.ludwiggj.finance.domain

import org.ludwiggj.finance.persistence.database.TransactionTuple
import org.ludwiggj.finance.persistence.file.PersistableToFile

case class Transaction(val holdingName: String, val date: FinanceDate, val description: String,
                  val in: Option[BigDecimal], val out: Option[BigDecimal], val priceDate: FinanceDate,
                  val priceInPounds: BigDecimal, val units: BigDecimal) extends PersistableToFile {

  def dateAsSqlDate = date.asSqlDate
  def priceDateAsSqlDate = priceDate.asSqlDate

  override def toString =
    s"Tx [holding: $holdingName, date: $date, description: $description, in: $in, out: $out, price date: $priceDate, price: $priceInPounds, units: $units]"

  def toFileFormat = s"$holdingName$separator$date$separator$description" +
    s"$separator${in.getOrElse("")}$separator${out.getOrElse("")}" +
    s"$separator$priceDate$separator$priceInPounds$separator$units"
}

object Transaction {

  def apply(tx: TransactionTuple) = {
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