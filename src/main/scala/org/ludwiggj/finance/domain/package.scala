package org.ludwiggj.finance

package object domain {
  val separator = '|'

  def parseNumber(candidateNumber: String) = {
    BigDecimal(stripNonFPDigits(candidateNumber))
  }

  def parseNumberOption(candidateNumber: String) = {
    val filteredNumber = stripNonFPDigits(candidateNumber)
    if (filteredNumber.size == 0) None else Some(BigDecimal(filteredNumber))
  }

  private def stripNonFPDigits(candidateNumber: String) = {
    candidateNumber filter (ch => "0123456789." contains ch)
  }

  def stripAllWhitespaceExceptSpace(str: String) = "[\\r\\n\\t]".r.replaceAllIn(str, "")

  def cleanHoldingName(name: String) = name.replaceAll("&amp;", "&").replaceAll("\\^", "").trim
}
