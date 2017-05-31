package models.org.ludwiggj.finance.domain

import TransactionType.{InvestmentLumpSum, InvestmentRegular, UnitShareConversionIn, UnitShareConversionOut}

case class HoldingSummary(val amountIn: BigDecimal, val unitsIn: BigDecimal,
                          private val unitsOutOption: Option[BigDecimal],
                          val price: Price) extends Ordered[HoldingSummary] {
  val zero = BigDecimal(0)

  val unitsOut = unitsOutOption.getOrElse(zero)
  val totalUnits = unitsIn - unitsOut
  val total = totalUnits * price.inPounds
  val delta = CashDelta(amountIn, total)

  override def toString = f"${price.fundName}%-50s £${amountIn}%8.2f" +
    f"  ${unitsIn}%10.4f ${unitsOut}%10.4f   ${totalUnits}%10.4f" +
    f"  ${price.date}  £${price.inPounds}%8.4f  £${total}%9.2f" +
    f"  £${delta.gain}%8.2f  ${delta.gainPct}%6.2f %%"

  def compare(that: HoldingSummary) = {
    val thisFundName = this.price.fundName
    val thatFundName = that.price.fundName

    thisFundName.compare(thatFundName)
  }
}

object HoldingSummary {

  def apply(transactions: List[Transaction], price: Price): HoldingSummary = {

    val amountIn = transactions
      .filter { tx => (tx.description == InvestmentRegular) || (tx.description == InvestmentLumpSum) }
      .flatMap(_.in).sum

    val unitsIn = transactions
      .filter { tx => tx.in.isDefined || (tx.description == UnitShareConversionIn) }
      .map(_.units).sum

    val unitsOut = transactions
      .filter { tx => tx.out.isDefined || (tx.description == UnitShareConversionOut) }
      .map(_.units).sum

    HoldingSummary(amountIn, unitsIn, Some(unitsOut), price)
  }
}