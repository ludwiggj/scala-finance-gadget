package org.ludwiggj

import scala.io.Source
import scala.Some
import scala.collection.JavaConversions._
import com.typesafe.config.ConfigFactory
import org.filippodeluca.ssoup.SSoup._

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
    java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF)

    val config = ConfigFactory.load("cofunds.conf")
    val accounts = config.getConfigList("site.accounts") map (Account(_))
    val account: Account = accounts(0)

    println(s"Logging in to ${config.getString("site.name")} as ${account.name}")

    val loginForm = LoginForm(config, page)
    val htmlContent = loginForm.login(account)

    parse(htmlContent.asXml)
  }

  def parsePageFromFile(fileName: String) = {
    val source = Source.fromFile(fileName, "UTF-8")
    parse(source.mkString)
  }
}