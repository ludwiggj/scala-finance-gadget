package models.org.ludwiggj.finance.domain

import org.joda.time.LocalDate

case class Portfolio(userName: String, private val date: LocalDate, private val holdings: HoldingSummaries) {
  val delta = CashDelta(holdings.amountIn, holdings.total)

  def holdingsIterator = holdings.iterator

  override def toString = holdings.toString
}