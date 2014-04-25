package org.ludwiggj.finance

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions._

object ConfigTest extends App {
  val config = ConfigFactory.load("site.conf")

  val formFields = config.getConfigList("site.login.form.fields") map (FormField(_))

  val accounts = config.getConfigList("site.accounts") map (Account(_))
  accounts.foreach(a => println(a))

  accounts(0).attributes.foreach {
     attr => println(attr.value)
  }

  val lookUp = Map(accounts(0).attributes map {attr => attr.unapply}: _*)

  println(lookUp)

  println(config.getString("site.baseUrl"))
  println(config.getString("site.login.form.submit"))
}