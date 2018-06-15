package models.org.ludwiggj.finance.web

import io.circe.literal._
import io.circe.{Decoder, DecodingFailure, Json}

// TODO - Move into scalaScratch repo
object CursorWorkout {

  case class Foo(firstName: String, lastName: String, age: Int, stuff: List[Boolean])

  def take1(json: Json): Unit = {
    implicit val fooDecoder: Decoder[Foo] = Decoder.instance { c =>
      c.focus.flatMap(_.asArray) match {
        case Some(fnJ +: lnJ +: rest) =>
          rest.reverse match {
            case ageJ +: stuffJ =>
              for {
                fn    <- fnJ.as[String]
                ln    <- lnJ.as[String]
                age   <- ageJ.as[Int]
                stuff <- Json.fromValues(stuffJ.reverse).as[List[Boolean]]
              } yield Foo(fn, ln, age, stuff)
            case _ => Left(DecodingFailure("Foo", c.history))
          }
        case None => Left(DecodingFailure("Foo", c.history))
      }
    }

    println(fooDecoder.decodeJson(json))
  }

  def take2(json: Json): Unit = {
    implicit val fooDecoder: Decoder[Foo] = Decoder.instance { c =>
      val fnC = c.downArray

      for {
        fn     <- fnC.as[String]
        lnC     = fnC.deleteGoRight
        ln     <- lnC.as[String]
        ageC    = lnC.deleteGoLast
        age    <- ageC.as[Int]
        stuffC  = ageC.delete
        stuff  <- stuffC.as[List[Boolean]]
      } yield Foo(fn, ln, age, stuff)
    }

    println(fooDecoder.decodeJson(json))
  }

  def main(args: Array[String]): Unit = {
    take1(json"""[ "Foo", "McBar", true, false, 137 ]""")
    take2(json"""[ "Foo", "McBar", true, false, 137 ]""")
  }
}
