package models.org.ludwiggj.finance.web

import io.circe._
import io.circe.generic.auto._
import io.circe.optics.JsonPath._
import io.circe.parser._
import io.circe.syntax._

// TODO - Move into scalaScratch repo
object JsonWorkout {

  def workout1(): Unit = {
    sealed trait Foo
    case class Bar(xs: Vector[String]) extends Foo
    case class Qux(i: Int, d: Option[Double]) extends Foo

    val foo: Foo = Qux(13, Some(14.0))

    val json = foo.asJson.noSpaces
    println(json)

    val decodedFoo = decode[Foo](json)
    println(decodedFoo)
  }

  def workout2(): Unit = {
    val json: Json = parse(
      """
{
  "order": {
    "customer": {
      "name": "Custy McCustomer",
      "contactDetails": {
        "address": "1 Fake Street, London, England",
        "phone": "0123-456-789"
      }
    },
    "items": [{
      "id": 123,
      "description": "banana",
      "quantity": 1
    }, {
      "id": 456,
      "description": "apple",
      "quantity": 2
    }],
    "total": 123.45
  }
}
""").getOrElse(Json.Null)

    val _phoneNum = root.order.customer.contactDetails.phone.string
    println(_phoneNum.getOption(json))
  }

  def main(args: Array[String]): Unit = {
    workout1()
    workout2()
  }
}
