package models.org.ludwiggj.finance.domain

object TransactionType extends Enumeration {
  type TransactionType = Value

  val InvestmentRegular = Value("Investment Regular")
  val InvestmentLumpSum = Value("Investment Lump Sum")
  val DividendReinvestment = Value("Dividend Reinvestment")
  val SaleForRegularPayment = Value("Sale for Regular Payment")
  val UnitShareConversionIn = Value("Unit/Share Conversion +")
  val UnitShareConversionOut = Value("Unit/Share Conversion -")
  val TaxReclaimReinvestment = Value("Tax Reclaim Reinvestment")
  val SwitchIn = Value("Switch In")
  val SwitchOut = Value("Switch Out")
}