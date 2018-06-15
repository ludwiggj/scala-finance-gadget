package utils

import org.joda.time.LocalDate
import play.api.mvc.PathBindable

object Binders {

   implicit def localDatePathBinder: PathBindable[LocalDate] = new PathBindable[LocalDate] {
      override def bind(key: String, value: String): Either[String, LocalDate] = {
         try {
            Right(LocalDate.parse(value))
         } catch {
            case _: IllegalArgumentException => Left("Date must be a joda LocalDate")
         }
      }
      override def unbind(key: String, value: LocalDate): String = value.toString
   }
}