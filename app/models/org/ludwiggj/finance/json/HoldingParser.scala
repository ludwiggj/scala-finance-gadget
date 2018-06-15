package models.org.ludwiggj.finance.json

import io.circe.optics.JsonPath.root
import io.circe.parser.parse
import io.circe.{Decoder, Encoder, HCursor, Json}
import models.org.ludwiggj.finance.domain._
import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

object HoldingParser {

  private def decoder(username: String, date: LocalDate): Decoder[Holding] = (c: HCursor) => for {
    name <- c.downField("name").as[String]
    units <- c.downField("units").as[String]
    price <- c.downField("price").downField("raw").as[BigDecimal]
  } yield {
    Holding(username, Price(FundName(name), date, price), BigDecimal(units.replaceAll(",", "")))
  }

  // Dummy Encoder - required for compilation, though I don't actually want to encode Holdings into json
  private def dummyEncoder: Encoder[Holding] = (_: Holding) => Json.obj()

  private def fromJson(json: Json, username: String): List[Holding] = {
    val dtf: DateTimeFormatter = DateTimeFormat.forPattern("dd MMMM yyyy")
    val datePath = root._data._data.formattedAsOfDate.string
    val maybeDate = datePath.getOption(json)
    val date = maybeDate.map(dtf.parseLocalDate).getOrElse(LocalDate.now())

    implicit val holdingDecoder: Decoder[Holding] = decoder(username, date)

    // Dummy Encoder - required for compilation, though I don't actually want to encode Holdings into json
    implicit val holdingEncoder: Encoder[Holding] = dummyEncoder

    root._data._data.details.each._data.as[Holding].getAll (json)
  }

  def fromJsonString(jsonString: String, username: String): List[Holding] = {
    fromJson(parse(jsonString).getOrElse(Json.Null), username)
  }
}