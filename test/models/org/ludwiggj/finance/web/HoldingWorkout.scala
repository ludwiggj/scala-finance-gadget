package models.org.ludwiggj.finance.web

import io.circe._
import io.circe.generic.auto._
import io.circe.optics.JsonPath._
import io.circe.parser._

import scala.io.Source

// TODO Rework
object HoldingWorkout {

  case class Hold(name: String, units: BigDecimal, price: BigDecimal)

  def holdings(): Unit = {

    val holdingsURL = getClass.getResource("/holdings.json")

    val holdingsFile = Source.fromURL(holdingsURL).getLines().mkString("\n")

    val json: Json = parse(holdingsFile).getOrElse((Json.Null))

    implicit val decodeHold: Decoder[Hold] = (c: HCursor) => for {
      name <- c.downField("name").as[String]
      units <- c.downField("units").as[String]
      price <- c.downField("price").downField("raw").as[BigDecimal]
    } yield {
      Hold(name, BigDecimal(units.replaceAll(",", "")), price)
    }

    val holdings = root._data._data.details.each._data.as[Hold].getAll(json)
    holdings.foreach(println)
  }

  def main(args: Array[String]): Unit = {
    holdings()
  }
}