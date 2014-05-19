package org.ludwiggj

import scala.io.Source
import org.filippodeluca.ssoup.SSoup._
import org.ludwiggj.finance.builders.LoginFormBuilder._
import scala.Some

package object finance {
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

  def loginAndParse(page: String) = {
    val loginForm = aLoginForm().basedOnConfig(WebSiteConfig("cofunds.conf")).loggingIntoPage(page).build()
    val htmlContent = loginForm.loginWithAccountAtIndex(0)

    val xml = htmlContent.asXml
    parse(xml)
  }

  def parsePageFromFile(fileName: String) = {
    val source = Source.fromFile(fileName, "UTF-8")
    parse(source.mkString)
  }
}