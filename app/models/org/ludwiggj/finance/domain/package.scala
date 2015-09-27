package models.org.ludwiggj.finance

import scala.language.implicitConversions

package object domain {
  val separator = '|'

  def parseNumber(candidateNumber: String) = {
    BigDecimal(stripNonFPDigits(candidateNumber))
  }

  def stripNonFPDigits(candidateNumber: String) = {
    candidateNumber filter (ch => "0123456789." contains ch)
  }

  def stripAllWhitespaceExceptSpace(str: String) = "[\\r\\n\\t]".r.replaceAllIn(str, "")
}