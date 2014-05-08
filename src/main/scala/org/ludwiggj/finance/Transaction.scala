package org.ludwiggj.finance

class Transaction(val holdingName: String, val date: FinanceDate, val description: String, val in: Option[BigDecimal],
                  val out: Option[BigDecimal], val priceDate: FinanceDate, val priceInPence: BigDecimal, val units: BigDecimal) {

  override def toString =
    s"Tx [holding: $holdingName, date: $date, description: $description, in: $in, out: $out, price date: $priceDate, price: $priceInPence, units: $units]"

  def toFileFormat = s"$holdingName${Transaction.separator}$date${Transaction.separator}$description" +
    s"${Transaction.separator}${in.getOrElse("")}${Transaction.separator}${out.getOrElse("")}" +
    s"${Transaction.separator}$priceDate${Transaction.separator}$priceInPence${Transaction.separator}$units"

  // This is likely to be a bit dodge e.g. subclasses etc.
  final override def equals(other: Any) = {
    val that =  if (other.isInstanceOf[Transaction]) other.asInstanceOf[Transaction] else null
    if (that == null) false
    else (
        (holdingName == that.holdingName) &&
        (date == that.date) &&
        (description == that.description) &&
        (in == that.in) &&
        (out == that.out) &&
        (priceDate == that.priceDate) &&
        (priceInPence == that.priceInPence) &&
        (units == that.units)
    )
  }

  def optionEquals(val1: Option[BigDecimal], val2: Option[BigDecimal]) = {
    (val1.isEmpty && val2.isEmpty) ||
      (val1.isDefined && val2.isDefined && (val1.get == val2.get))
  }

  final override def hashCode = {
    var result = 17
    result = 31 * result + holdingName.hashCode
    result = 31 * result + date.hashCode
    result = 31 * result + description.hashCode
    result = 31 * result + in.hashCode
    result = 31 * result + out.hashCode
    result = 31 * result + priceDate.hashCode
    result = 31 * result + priceInPence.hashCode
    result = 31 * result + units.hashCode
    result
  }
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