package models.org.ludwiggj.finance.domain

sealed trait TransactionType {
  def name: String

  @Override
  override def toString: String = name
}

case object AdminCharge extends TransactionType {
  val name = "Admin Charge"
}

case object CorrectiveTradeAdjustment extends TransactionType {
  val name = "Corrective Trade Adjustment"
}

case object Distribution extends TransactionType {
  val name = "Distribution"
}

case object DividendReinvestment extends TransactionType {
  val name = "Dividend Reinvestment"
}

case object InvestmentLumpSum extends TransactionType {
  val name = "Investment Lump Sum"
}

case object InvestmentRegular extends TransactionType {
  val name = "Investment Regular"
}

case object OngoingAdviserCharge extends TransactionType {
  val name = "Ongoing Adviser Charge"
}

case object PendingTradeIn extends TransactionType {
  val name = "Pending trade in"
}

case object PendingTradeOut extends TransactionType {
  val name = "Pending trade out"
}

case object SaleForRegularPayment extends TransactionType {
  val name = "Sale for Regular Payment"
}

case object SaleRequest extends TransactionType {
  val name = "Sale Request"
}

case object SwitchIn extends TransactionType {
  val name = "Switch In"
}

case object SwitchOut extends TransactionType {
  val name = "Switch Out"
}

object TaxReclaimReinvestment extends TransactionType {
  val name = "Tax Reclaim Reinvestment"
}

case object TransferIn extends TransactionType {
  val name = "Transfer in"
}

case object TransferOut extends TransactionType {
  val name = "Transfer out of fund"
}

case object UnitShareConversionIn extends TransactionType {
  val name = "Unit/Share Conversion +"
}

case object UnitShareConversionOut extends TransactionType {
  val name = "Unit/Share Conversion -"
}

case object Unspecified extends TransactionType {
  val name = "Unspecified"
}

object TransactionType {
  def aTransactionType(value: String): TransactionType = {
    Vector(
      AdminCharge, CorrectiveTradeAdjustment, Distribution, DividendReinvestment,
      InvestmentLumpSum, InvestmentRegular, OngoingAdviserCharge, PendingTradeIn,
      PendingTradeOut, SaleForRegularPayment, SaleRequest, SwitchIn, SwitchOut,
      TaxReclaimReinvestment, TransferIn, TransferOut, UnitShareConversionIn,
      UnitShareConversionOut
    ).find(_.name == value).getOrElse(Unspecified)
  }
}