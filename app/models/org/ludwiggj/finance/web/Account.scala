package models.org.ludwiggj.finance.web

import com.typesafe.config.Config

import scala.collection.JavaConversions._

class Account(val name: String, private val attributes: List[Attribute]) {
  override def toString = s"Account (name: $name attributes:\n  ${attributes.mkString("\n  ")}\n)"
  private val attributeValueMap = Map(
    attributes map { attr => attr.unapply }: _*
  )
  def attributeValue(name: String) = attributeValueMap(name)
}

object Account {
    def apply(config: Config) = new Account(
      config.getString("name"),
      (config.getConfigList("attributes") map (Attribute(_))).toList
    )
}