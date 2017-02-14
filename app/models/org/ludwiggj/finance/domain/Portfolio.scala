package models.org.ludwiggj.finance.domain

case class Portfolio(val userName: String, private val date: FinanceDate, private val holdings: HoldingSummaries) {
  val delta = CashDelta(holdings.amountIn, holdings.total)

  def holdingsIterator = holdings.iterator

  override def toString = holdings.toString
}