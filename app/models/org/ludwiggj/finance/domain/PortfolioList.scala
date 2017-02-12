package models.org.ludwiggj.finance.domain

// TODO - portfolios should be private
// TODO - search and replace occurrences of List[Portfolio]
case class PortfolioList(val portfolios: List[Portfolio]) {
  // TODO - may not need date
  val date: FinanceDate = portfolios.head.date
  val delta = portfolios.foldRight(CashDelta())(
    (portfolio, delta) => delta.add(portfolio.delta)
  )
  def iterator = portfolios.iterator
}