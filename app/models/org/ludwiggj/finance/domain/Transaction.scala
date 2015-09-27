package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.database.TransactionTuple
import models.org.ludwiggj.finance.persistence.file.PersistableToFile

case class Transaction(val userName: String, val date: FinanceDate, val description: String, val in: Option[BigDecimal],
                       val out: Option[BigDecimal], val price: Price, val units: BigDecimal) extends PersistableToFile {

  def fundName = price.fundName

  def priceDate = price.date

  def priceInPounds = price.inPounds

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
    result = prime * result + (if (! in.isDefined) 0 else in.hashCode)
    result = prime * result + (if (! out.isDefined) 0 else out.hashCode)
    result = prime * result + (if (priceDate == null) 0 else priceDate.hashCode)
    result = prime * result + units.intValue();
    return result
  }
}

object Transaction {

  private def parseNumberOption(candidateNumber: String) = {
      val filteredNumber = stripNonFPDigits(candidateNumber)
      if (filteredNumber.size == 0) None else Some(BigDecimal(filteredNumber))
    }

  def apply(userName:String, tx: TransactionTuple) = {
    val (fundName, date, description, in, out, priceDate, priceInPounds, units) = tx
    new Transaction(userName, FinanceDate(date), description, Option(in), Option(out),
      Price(fundName, priceDate, priceInPounds), units)
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

    val priceInPounds = parseNumber(priceInPence) / 100;

    Transaction(userName, FinanceDate(date), description.trim, parseNumberOption(in), parseNumberOption(out),
      Price(fundName, FinanceDate(priceDate), priceInPounds), parseNumber(units))
  }

  def apply(userName: String, row: Array[String]): Transaction = {
    Transaction(userName, FinanceDate(row(1)), row(2), parseNumberOption(row(3)), parseNumberOption(row(4)),
      Price(row(0), row(5), row(6)), parseNumber(row(7)))
  }
}
