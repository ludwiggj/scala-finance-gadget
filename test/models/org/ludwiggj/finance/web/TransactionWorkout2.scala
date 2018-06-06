package models.org.ludwiggj.finance.web

import io.circe._
import io.circe.literal._
import models.org.ludwiggj.finance.domain.{FundName, Price, TransactionType}
import org.joda.time.LocalDate

object TransactionWorkout2 {

  case class Tx(date: LocalDate,
                description: TransactionType,
                in: Option[BigDecimal],
                out: Option[BigDecimal],
                price: Price,
                units: BigDecimal
               )

  def main(args: Array[String]): Unit = {

    implicit val decodeTx: Decoder[Tx] = (c: HCursor) => {
      val dateC1 = c.downField("date").downArray

      for {
        name <- c.downField("name").as[String]
        year <- dateC1.as[Int]
        dateC2 = dateC1.deleteGoRight
        month <- dateC2.as[Int]
        dateC3 = dateC2.deleteGoRight
        dateOfMonth <- dateC3.as[Int]
        date = new LocalDate(year, month, dateOfMonth)
        amount <- c.downField("amount").downField("raw").as[BigDecimal]
        fundName <- c.downField("fundName").as[String]
        unitPrice <- c.downField("unitPrice").downField("raw").as[BigDecimal]
        numberOfUnits <- c.downField("numberOfUnits").as[BigDecimal]
      } yield {
        Tx(
          date,
          TransactionType.aTransactionType(name),
          if (amount > 0) Some(amount) else None,
          if (amount < 0) Some(amount.abs) else None,
          Price(FundName(fundName), date, unitPrice),
          numberOfUnits.abs
        )
      }
    }

    val jsonTx =
      json"""{
      "name" : "Admin Charge",
      "fundName" : "Coffee",
      "amount" : {
        "raw" : 3.5
      },
      "date": [2017, 12, 25],
      "numberOfUnits" : 2.56,
      "unitPrice" : {
        "raw" : 1.23
      }
    }"""

    val decodedTx: Decoder.Result[Tx] = decodeTx.decodeJson(jsonTx)

    println(decodedTx)
  }
}