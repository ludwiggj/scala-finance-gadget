package models.org.ludwiggj.finance.domain

case class Portfolio(val userName: String, val date: FinanceDate, val holdings: List[HoldingSummary]) {
  val delta = CashDelta(holdings.map(_.amountIn).sum, holdings.map(_.total).sum)

  override def toString = holdings.foldLeft("")(
    (str: String, holdingSummary: HoldingSummary) => str + holdingSummary + "\n"
  )
}