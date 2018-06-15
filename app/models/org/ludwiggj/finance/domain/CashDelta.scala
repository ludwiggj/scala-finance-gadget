package models.org.ludwiggj.finance.domain

case class CashDelta(amountIn: BigDecimal = 0, total: BigDecimal = 0) {
  def add(delta: CashDelta) = CashDelta(amountIn + delta.amountIn, total + delta.total)

  private val zero = BigDecimal(0)

  val gain: BigDecimal = total - amountIn

  val gainPct: BigDecimal = if ((gain != zero) && (amountIn != 0)) {
    100 * gain / amountIn
  } else zero

  private val spacer: String = " " * 61

  override def toString = f"£$amountIn%9.2f${spacer}£$total%9.2f £$gain%9.2f  $gainPct%6.2f %%"
}