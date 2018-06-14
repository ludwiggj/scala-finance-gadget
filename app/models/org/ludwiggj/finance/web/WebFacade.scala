package models.org.ludwiggj.finance.web

import java.net.URL

import com.gargoylesoftware.htmlunit.html.HtmlInput
import com.gargoylesoftware.htmlunit.{HttpMethod, WebRequest}
import com.typesafe.config.ConfigFactory
import io.circe.literal._
import io.circe.optics.JsonPath.root
import io.circe.parser.parse
import io.circe.{Decoder, Encoder, HCursor, Json}
import models.org.ludwiggj.finance.domain._
import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

class WebFacade(val user: User) {
  private val config = ConfigFactory.load("acme2")
  private val baseUrl = urlConfigItem("base")
  private val loginUrl = url("login")
  private val logoutUrl = url("logout")

  private val webClient = WebClient()

  private def urlConfigItem(configItem: String): String = {
    config.getString("site.url." + configItem)
  }

  private def url(configItem: String): String = {
    s"$baseUrl${urlConfigItem(configItem)}"
  }

  private def login(): Unit = {
    println(s"Logging into $loginUrl as ${user.username}")
    val html = webClient.getPage(loginUrl)
    val form = html.getForms.head
    form.getInputByName("username").asInstanceOf[HtmlInput].setValueAttribute(user.username)
    form.getInputByName("password").asInstanceOf[HtmlInput].setValueAttribute(user.password)
    form.getElementsByTagName("button").get(0).click()
  }

  private def logout(): Unit = {
    webClient.getPage(loginUrl)
  }

  private def dataByPost(url: String): String = {
    val wr = new WebRequest(new URL(url), HttpMethod.POST)
    wr.setAdditionalHeader("Content-Type", "application/json; charset=utf-8")
    wr.setAdditionalHeader("Accept", "application/json, text/javascript, */*; q=0.01")
    webClient.getPage(wr)
  }

  private def jsonToHoldings(json: Json): List[Holding] = {
    val dtf: DateTimeFormatter = DateTimeFormat.forPattern("dd MMMM yyyy")
    val datePath = root._data._data.formattedAsOfDate.string
    val maybeDate = datePath.getOption(json)
    val date = maybeDate.map(dtf.parseLocalDate(_)).getOrElse(LocalDate.now())

    implicit val decodeHold: Decoder[Holding] = (c: HCursor) => for {
      name <- c.downField("name").as[String]
      units <- c.downField("units").as[String]
      price <- c.downField("price").downField("raw").as[BigDecimal]
    } yield {
      Holding(user.reportName, Price(FundName(name), date, price), BigDecimal(units.replaceAll(",", "")))
    }

    // Dummy Encoder - required for compilation, though I don't actually want to encode Holdings into json
    implicit val encodeHolding: Encoder[Holding] = new Encoder[Holding] {
      final def apply(h: Holding): Json = Json.obj()
    }

    root._data._data.details.each._data.as[Holding].getAll(json)
  }

  def getHoldings(): List[Holding] = {
    login()
    val holdings = parse(dataByPost(s"${url("holdings")}${user.accountId}")).getOrElse((Json.Null))
    logout()
    jsonToHoldings(holdings).filter(
      h => (h.name != FundName("Cash")) && (h.name != FundName("Pending Trades"))
    )
  }

  private def jsonToTransactions(json: Json): List[Transaction] = {
    val dtf: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yy")

    implicit val decodeTx: Decoder[Transaction] = (c: HCursor) => for {
      name <- c.downField("name").as[String]
      dateStr <- c.downField("dateString").as[String]
      amount <- c.downField("amount").downField("raw").as[BigDecimal]
      date = dtf.parseLocalDate(dateStr)
      fundName <- c.downField("fundName").as[String]
      unitPrice <- c.downField("unitPrice").downField("raw").as[BigDecimal]
      numberOfUnits <- c.downField("numberOfUnits").as[BigDecimal]
    } yield {
      Transaction(
        user.reportName,
        date,
        TransactionCategory.aTransactionCategory(name),
        if (amount > 0) Some(amount) else None,
        if (amount < 0) Some(amount.abs) else None,
        Price(FundName(fundName), date, unitPrice),
        numberOfUnits.abs
      )
    }

    // Dummy Encoder - required for compilation, though I don't actually want to encode Transactions into json
    implicit val encodeTx: Encoder[Transaction] = new Encoder[Transaction] {
      final def apply(tx: Transaction): Json = Json.obj()
    }

    root._data.transactions.each.as[Transaction].getAll(json)
  }

  def getTransactions(): List[Transaction] = {
    login()
    val transactions = parse(dataByPost(s"${url("transactions")}${user.accountId}")).getOrElse(Json.Null)
    logout()
    jsonToTransactions(transactions)
  }
}

object WebFacade {
  def apply(user: User): WebFacade = {
    new WebFacade(user)
  }
}