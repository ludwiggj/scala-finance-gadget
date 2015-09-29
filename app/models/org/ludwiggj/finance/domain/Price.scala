package models.org.ludwiggj.finance.domain

case class Price(val fundName: FundName, val date: FinanceDate, val inPounds: BigDecimal) {

  override def toString =
    s"Price [name: $fundName, date: $date, price: £$inPounds]"
}

object Price {
  def apply(row: Array[String]): Price = {
    Price(row(0), row(1), row(2))
  }
}