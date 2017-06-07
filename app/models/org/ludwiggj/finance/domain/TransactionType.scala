package models.org.ludwiggj.finance.domain

import scala.language.implicitConversions

sealed trait TransactionType {
  def name: String

  @Override
  override def toString: String = name
}

case object InvestmentRegular extends TransactionType {
  val name = "Investment Regular"
}

case object InvestmentLumpSum extends TransactionType {
  val name = "Investment Lump Sum"
}

case object DividendReinvestment extends TransactionType {
  val name = "Dividend Reinvestment"
}

case object SaleForRegularPayment extends TransactionType {
  val name = "Sale for Regular Payment"
}

case object UnitShareConversionIn extends TransactionType {
  val name = "Unit/Share Conversion +"
}

case object UnitShareConversionOut extends TransactionType {
  val name = "Unit/Share Conversion -"
}

case object TaxReclaimReinvestment extends TransactionType {
  val name = "Tax Reclaim Reinvestment"
}

case object SwitchIn extends TransactionType {
  val name = "Switch In"
}

case object SwitchOut extends TransactionType {
  val name = "Switch Out"
}

case object Unspecified extends TransactionType {
  val name = "Unspecified"
}

object TransactionType {
  implicit def fromString(value: String): TransactionType = {
    Vector(
      InvestmentRegular, InvestmentLumpSum, DividendReinvestment, SaleForRegularPayment,
      UnitShareConversionIn, UnitShareConversionOut, TaxReclaimReinvestment, SwitchIn, SwitchOut
    ).find(_.name == value).getOrElse(Unspecified)
  }
}