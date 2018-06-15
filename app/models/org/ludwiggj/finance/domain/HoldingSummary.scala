package models.org.ludwiggj.finance.domain

case class HoldingSummary(amountIn: BigDecimal, unitsIn: BigDecimal,
                          private val unitsOutOption: Option[BigDecimal],
                          price: Price) extends Ordered[HoldingSummary] {
  val unitsOut: BigDecimal = unitsOutOption.getOrElse(BigDecimal(0))
  val totalUnits: BigDecimal = unitsIn - unitsOut
  val total: BigDecimal = totalUnits * price.inPounds
  val delta: CashDelta = CashDelta(amountIn, total)

  override def toString: String = f"${price.fundName}%-50s £$amountIn%8.2f" +
    f"  $unitsIn%10.4f $unitsOut%10.4f   $totalUnits%10.4f" +
    f"  ${price.date}  £${price.inPounds}%8.4f  £$total%9.2f" +
    f"  £${delta.gain}%8.2f  ${delta.gainPct}%6.2f %%"

  def compare(that: HoldingSummary): Int = {
    val thisFundName = this.price.fundName
    val thatFundName = that.price.fundName

    thisFundName.compare(thatFundName)
  }
}

object HoldingSummary {

  def apply(transactions: List[Transaction], price: Price): HoldingSummary = {

    val amountIn = transactions
      .filter(_.isCashIn)
      .flatMap(_.in).sum

    val unitsIn = transactions
      .filter(_.isUnitsIn)
      .map(_.units).sum

    val unitsOut = transactions
      .filter(_.isUnitsOut)
      .map(_.units).sum

    HoldingSummary(amountIn, unitsIn, Some(unitsOut), price)
  }
}