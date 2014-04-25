package org.ludwiggj.finance

import com.typesafe.config.Config

class FormField(val name: String, val htmlName: String) {
  override def toString = s"FormField (name: $name htmlName: $htmlName)"
}

object FormField {
  def apply(config: Config) = new FormField(config.getString("name"), config.getString("htmlName"))
}