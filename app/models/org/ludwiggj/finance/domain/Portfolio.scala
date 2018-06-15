package models.org.ludwiggj.finance.domain

import org.joda.time.LocalDate

case class Portfolio(userName: String, private val date: LocalDate, private val holdings: HoldingSummaryList) {
  val delta: CashDelta = CashDelta(holdings.amountIn, holdings.total)

  def holdingsIterator: Iterator[HoldingSummary] = holdings.iterator

  override def toString: String = holdings.toString
}