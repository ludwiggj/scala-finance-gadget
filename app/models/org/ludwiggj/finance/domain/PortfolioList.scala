package models.org.ludwiggj.finance.domain

import scala.language.postfixOps

case class PortfolioList(private val portfolios: List[Portfolio]) {
  val delta = portfolios.foldRight(CashDelta())(
    (portfolio, delta) => delta.add(portfolio.delta)
  )

  def iterator = portfolios.iterator
}