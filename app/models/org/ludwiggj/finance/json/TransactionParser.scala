package models.org.ludwiggj.finance.json

import io.circe.optics.JsonPath.root
import io.circe.parser.parse
import io.circe.{Decoder, Encoder, HCursor, Json}
import models.org.ludwiggj.finance.domain.{FundName, Price, Transaction, TransactionCategory}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

object TransactionParser {

  private def decoder(username: String): Decoder[Transaction] = (c: HCursor) => {
    val dtf: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yy")
    for {
      name <- c.downField("name").as[String]
      dateStr <- c.downField("dateString").as[String]
      amount <- c.downField("amount").downField("raw").as[BigDecimal]
      date = dtf.parseLocalDate(dateStr)
      fundName <- c.downField("fundName").as[String]
      unitPrice <- c.downField("unitPrice").downField("raw").as[BigDecimal]
      numberOfUnits <- c.downField("numberOfUnits").as[BigDecimal]
    } yield {
      Transaction(
        username,
        date,
        TransactionCategory.aTransactionCategory(name),
        if (amount > 0) Some(amount) else None,
        if (amount < 0) Some(amount.abs) else None,
        Price(FundName(fundName), date, unitPrice),
        numberOfUnits.abs
      )
    }
  }

  // Required for compilation, though I don't actually want to encode Transactions into json
  private def dummyEncoder: Encoder[Transaction] = (_: Transaction) => Json.obj()

  private def fromJson(json: Json, username: String): List[Transaction] = {
    implicit val txDecoder: Decoder[Transaction] = decoder(username)

    // Dummy Encoder - required for compilation, though I don't actually want to encode Transactions into json
    implicit val txEncoder: Encoder[Transaction] = dummyEncoder

    root._data.transactions.each.as[Transaction].getAll(json)
  }

  def fromJsonString(jsonString: String, username: String): List[Transaction] = {
    fromJson(parse(jsonString).getOrElse(Json.Null), username)
  }
}
