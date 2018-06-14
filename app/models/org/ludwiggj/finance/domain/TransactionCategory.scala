package models.org.ludwiggj.finance.domain

sealed trait TransactionCategory {
  def name: String
  def shouldBePersistedIntoDb: Boolean = true
  def isCashIn: Boolean = false

  override def toString: String = name
}

case object AdminCharge extends TransactionCategory {
  val name = "Admin Charge"
  override def shouldBePersistedIntoDb: Boolean = false
}

case object CorrectiveTradeAdjustment extends TransactionCategory {
  val name = "Corrective Trade Adjustment"
}

case object Distribution extends TransactionCategory {
  val name = "Distribution"
  override def shouldBePersistedIntoDb: Boolean = false
}
case object DistributionReinvestment extends TransactionCategory {
  val name = "Distribution Reinvestment"
  override def shouldBePersistedIntoDb: Boolean = false
}

case object DividendReinvestment extends TransactionCategory {
  val name = "Dividend Reinvestment"
}

case object FundPurchase extends TransactionCategory {
  val name = "Fund purchase"
}

case object InvestmentLumpSum extends TransactionCategory {
  val name = "Investment Lump Sum"
  override def isCashIn: Boolean = true
}

case object InvestmentRegular extends TransactionCategory {
  val name = "Investment Regular"
  override def isCashIn: Boolean = true
}

case object MoveMoney extends TransactionCategory {
  val name = "Move money"
  override def shouldBePersistedIntoDb: Boolean = false
}

case object OngoingAdviserCharge extends TransactionCategory {
  val name = "Ongoing Adviser Charge"
  override def shouldBePersistedIntoDb: Boolean = false
}

case object PendingTradeIn extends TransactionCategory {
  val name = "Pending trade in"
  override def shouldBePersistedIntoDb: Boolean = false
}

case object PendingTradeOut extends TransactionCategory {
  val name = "Pending trade out"
  override def shouldBePersistedIntoDb: Boolean = false
}

case object PurchaseRequest extends TransactionCategory {
  val name = "Purchase Request"
  override def shouldBePersistedIntoDb: Boolean = false
}

case object SaleForRegularPayment extends TransactionCategory {
  val name = "Sale for Regular Payment"
}

case object SaleRequest extends TransactionCategory {
  val name = "Sale Request"
  override def shouldBePersistedIntoDb: Boolean = false
}

case object SwitchIn extends TransactionCategory {
  val name = "Switch In"
}

case object SwitchOut extends TransactionCategory {
  val name = "Switch Out"
}

object TaxReclaimReinvestment extends TransactionCategory {
  val name = "Tax Reclaim Reinvestment"
}

case object TransferIn extends TransactionCategory {
  val name = "Transfer in"
}

case object TransferOut extends TransactionCategory {
  val name = "Transfer out of fund"
}

case object UnitShareConversionIn extends TransactionCategory {
  val name = "Unit/Share Conversion +"
}

case object UnitShareConversionOut extends TransactionCategory {
  val name = "Unit/Share Conversion -"
}

case object Unspecified extends TransactionCategory {
  val name = "Unspecified"
}

object TransactionCategory {
  def aTransactionCategory(value: String): TransactionCategory = {
    Vector(
      AdminCharge, CorrectiveTradeAdjustment, Distribution, DistributionReinvestment,
      DividendReinvestment, FundPurchase, InvestmentLumpSum, InvestmentRegular,
      MoveMoney, OngoingAdviserCharge, PendingTradeIn, PendingTradeOut,
      PurchaseRequest, SaleForRegularPayment, SaleRequest, SwitchIn, SwitchOut,
      TaxReclaimReinvestment, TransferIn, TransferOut, UnitShareConversionIn,
      UnitShareConversionOut
    ).find(_.name == value).getOrElse(Unspecified)
  }
}