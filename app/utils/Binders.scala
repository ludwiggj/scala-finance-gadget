package utils

import play.api.mvc.PathBindable
import java.sql.Date

object Binders {
   implicit def sqlDatePathBinder = new PathBindable[Date] {
      override def bind(key: String, value: String): Either[String, Date] = {
         try {
            Right(Date.valueOf(value))
         } catch {
            case e: IllegalArgumentException => Left("Date must be a sql Date")
         }
      }
      override def unbind(key: String, value: Date): String = value.toString
   }
}