package models.org.ludwiggj.finance.web

import io.circe._
import io.circe.optics.JsonPath._
import io.circe.parser._
import models.org.ludwiggj.finance.domain.{AdminCharge, FundName, Price, TransactionCategory}
import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

import scala.io.Source

object TransactionWorkout {

  case class Tx(date: LocalDate,
                description: TransactionCategory,
                in: Option[BigDecimal],
                out: Option[BigDecimal],
                price: Price,
                units: BigDecimal
               )

  def transactions(): Unit = {

    val txURL = getClass.getResource("/transactions.json")

    val txFile = Source.fromURL(txURL).getLines().mkString("\n")

    val json: Json = parse(txFile).getOrElse((Json.Null))

    val dtf: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yy")

    implicit val decodeTx: Decoder[Tx] = (c: HCursor) => for {
      name <- c.downField("name").as[String]
      dateStr <- c.downField("dateString").as[String]
      amount <- c.downField("amount").downField("raw").as[BigDecimal]
      date = dtf.parseLocalDate(dateStr)
      fundName <- c.downField("fundName").as[String]
      unitPrice <- c.downField("unitPrice").downField("raw").as[BigDecimal]
      numberOfUnits <- c.downField("numberOfUnits").as[BigDecimal]
    } yield {
      Tx(
        date,
        TransactionCategory.aTransactionCategory(name),
        if (amount > 0) Some(amount) else None,
        if (amount < 0) Some(amount.abs) else None,
        Price(FundName(fundName), date, unitPrice),
        numberOfUnits.abs
      )
    }

    implicit val encodeTx: Encoder[Tx] = new Encoder[Tx] {
      final def apply(tx: Tx): Json = Json.obj(
        ("name", Json.fromString(tx.description.toString)),
        ("fundName", Json.fromString(tx.price.fundName.toString)),
        ("amount", Json.obj(
          ("raw", Json.fromBigDecimal(tx.in.getOrElse(tx.out.getOrElse(0))))
        )),
        ("dateString", Json.fromString(dtf.print(tx.date))),
        ("numberOfUnits", Json.fromBigDecimal(tx.units)),
        ("unitPrice", Json.obj(
          ("raw", Json.fromBigDecimal(tx.price.inPounds))
        ))
      )
    }

    val txs = root._data.transactions.each.as[Tx].getAll(json)
    txs.filter(tx => tx.in.isDefined || tx.out.isDefined).foreach(println)

    val now = LocalDate.now()
    val tx = Tx(
      now,
      AdminCharge,
      Some(3.5),
      None,
      Price(FundName("Coffee"), now, 1.23),
      2.56)

    println(tx)

    val encodedTx = encodeTx.apply(tx)

    println(encodedTx)

    val decodedTx: Decoder.Result[Tx] = decodeTx.decodeJson(encodedTx)

    assert(decodedTx.right.get == tx)
  }

  def main(args: Array[String]): Unit = {
    transactions()
  }
}
