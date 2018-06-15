package models.org.ludwiggj.finance

import scala.math.BigDecimal.RoundingMode

package object domain {
  val separator = '|'

  def aBigDecimal(candidateNumber: String): BigDecimal = {
    BigDecimal(stripNonFPDigits(candidateNumber))
  }

  def stripNonFPDigits(candidateNumber: String): String = {
    candidateNumber filter (ch => "0123456789." contains ch)
  }

  def stripAllWhitespaceExceptSpace(str: String): String = "[\\r\\n\\t]".r.replaceAllIn(str, "")

  def scaled(value: BigDecimal, numberOfDecimalPlaces: Int): BigDecimal = {
    value.setScale(numberOfDecimalPlaces, RoundingMode.HALF_EVEN)
  }
}