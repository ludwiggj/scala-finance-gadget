package utils

import org.joda.time.LocalDate
import play.api.mvc.PathBindable

object Binders {

   implicit def localDatePathBinder = new PathBindable[LocalDate] {
      override def bind(key: String, value: String): Either[String, LocalDate] = {
         try {
            Right(LocalDate.parse(value))
         } catch {
            case e: IllegalArgumentException => Left("Date must be a joda LocalDate")
         }
      }
      override def unbind(key: String, value: LocalDate): String = value.toString
   }
}