package models.org.ludwiggj.finance.web

import java.net.URL
import com.gargoylesoftware.htmlunit.html.HtmlInput
import com.gargoylesoftware.htmlunit.{HttpMethod, Page, WebRequest}

object NewSiteWorkout {
  def main(args: Array[String]): Unit = {

    val html = WebClient.getPage("https://customerdashboard.aegon.co.uk/login")

    val form = html.getForms.head

    val input1 = form.getInputByName("username").asInstanceOf[HtmlInput]
    println(s"FORM FIELD 1 >>>>>\n${input1.toString}\n<<<<<<")

    val input2 = form.getInputByName("password").asInstanceOf[HtmlInput]
    println(s"FORM FIELD 2 >>>>>\n${input2.toString}\n<<<<<<")

    input1.setValueAttribute("graeme.ludwig@btopenworld.com")
    input2.setValueAttribute("C8sh4Scr8p")

    val submitButton = form.getElementsByTagName("button").get(0)

    val res = submitButton.click().asInstanceOf[Page]

    println("Result\n\n\n" + res)

    val wrHoldings = new WebRequest(
      new URL("https://customerdashboard.aegon.co.uk/rest/external/view/hateoas/savings/investment-overview/80509341"),
      HttpMethod.POST
    )
    wrHoldings.setAdditionalHeader("Content-Type", "application/json; charset=utf-8")
    wrHoldings.setAdditionalHeader("Accept", "application/json, text/javascript, */*; q=0.01")

    println("Here are the holdings...\n\n")
    println(WebClient.getPage(wrHoldings))

    val wrTxs = new WebRequest(
      new URL("https://customerdashboard.aegon.co.uk/rest/external/view/hateoas/savings/detailed-transactions/80509341"),
      HttpMethod.POST
    )
    wrTxs.setAdditionalHeader("Content-Type", "application/json; charset=utf-8")
    wrTxs.setAdditionalHeader("Accept", "application/json, text/javascript, */*; q=0.01")

    println("Here are the txs...\n\n")
    println(WebClient.getPage(wrTxs))
  }
}