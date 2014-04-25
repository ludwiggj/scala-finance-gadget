package org.ludwiggj.finance

import scala.util.matching.Regex

class Transaction(val holdingName: String, val date: FinanceDate, val description: String, val in: Option[BigDecimal],
                  val out: Option[BigDecimal], val priceDate: FinanceDate, val priceInPence: BigDecimal, val units: BigDecimal) {

  override def toString =
    s"Tx [holding: $holdingName, date: $date, description: $description, in: $in, out: $out, price date: $priceDate, price: $priceInPence, units: $units]"

  def toFileFormat = s"$holdingName${Transaction.separator}$date${Transaction.separator}$description" +
    s"${Transaction.separator}${in.getOrElse("")}${Transaction.separator}${out.getOrElse("")}" +
    s"${Transaction.separator}$priceDate${Transaction.separator}$priceInPence${Transaction.separator}$units"
}

object Transaction {
  val separator = '|'

  def apply(holdingName: String,  date: FinanceDate, description: String, in: Option[BigDecimal],
                    out: Option[BigDecimal], priceDate: FinanceDate, priceInPence: BigDecimal, units: BigDecimal)  =
    new Transaction(holdingName, date, description, in, out, priceDate, priceInPence, units)

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

    Transaction(cleanHoldingName(holdingName), FinanceDate(date), description.trim,
      parseNumberOption(in), parseNumberOption(out), FinanceDate(priceDate), parseNumber(priceInPence), parseNumber(units))
  }

  def apply(row: Array[String]): Transaction = {
    Transaction(row(0), FinanceDate(row(1)), row(2), parseNumberOption(row(3)), parseNumberOption(row(4)),
      FinanceDate(row(5)), parseNumber(row(6)), parseNumber(row(7)))
  }
}