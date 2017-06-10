package models.org.ludwiggj.finance

package object domain {
  val separator = '|'

  def aBigDecimal(candidateNumber: String): BigDecimal = {
    BigDecimal(stripNonFPDigits(candidateNumber))
  }

  def stripNonFPDigits(candidateNumber: String) = {
    candidateNumber filter (ch => "0123456789." contains ch)
  }

  def stripAllWhitespaceExceptSpace(str: String) = "[\\r\\n\\t]".r.replaceAllIn(str, "")
}