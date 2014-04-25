package org.ludwiggj.finance

import com.typesafe.config.Config

class Attribute(val name: String, val value: String) {
  override def toString = s"Attribute (name: $name value: $value)"
  def unapply() = (name, value)
}

object Attribute {
  def apply(config: Config) = new Attribute(config.getString("name"), config.getString("value"))
}