package org.ludwiggj.finance.domain

import org.ludwiggj.finance.persistence.database.TransactionTuple
import org.ludwiggj.finance.persistence.file.PersistableToFile

case class Transaction(val date: FinanceDate, val description: String, val in: Option[BigDecimal],
                       val out: Option[BigDecimal], val price: Price, val units: BigDecimal) extends PersistableToFile {

  def dateAsSqlDate = date.asSqlDate

  def holdingName = price.holdingName

  def priceDate = price.date

  def priceDateAsSqlDate = price.dateAsSqlDate

  def priceInPounds = price.inPounds

  override def toString =
    s"Tx [holding: ${price.holdingName}, date: $date, description: $description, in: $in, out: $out, " +
      s"price date: ${price.date}, price: ${price.inPounds}, units: $units]"

  def toFileFormat = s"${price.holdingName}$separator$date$separator$description" +
    s"$separator${in.getOrElse("")}$separator${out.getOrElse("")}" +
    s"$separator${price.date}$separator${price.inPounds}$separator$units"
}

object Transaction {

  def apply(tx: TransactionTuple) = {
    val (holdingName, date, description, in, out, priceDate, priceInPounds, units) = tx
    new Transaction(FinanceDate(date), description, Option(in), Option(out),
      Price(holdingName, priceDate, priceInPounds), units)
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

    Transaction(FinanceDate(date), description.trim, parseNumberOption(in), parseNumberOption(out),
      Price(cleanHoldingName(holdingName), FinanceDate(priceDate), priceInPounds), parseNumber(units))
  }

  def apply(row: Array[String]): Transaction = {
    Transaction(FinanceDate(row(1)), row(2), parseNumberOption(row(3)), parseNumberOption(row(4)),
      Price(row(0), row(5), row(6)), parseNumber(row(7)))
  }
}
