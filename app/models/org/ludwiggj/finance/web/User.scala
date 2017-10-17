package models.org.ludwiggj.finance.web

import com.typesafe.config.Config

import scala.collection.JavaConverters._

class User private(val name: String, val reportName: String, private val attributes: List[Attribute]) {
  override def toString =
    s"User (name: $name, reportName: $reportName, attributes:\n  ${attributes.mkString("\n  ")}\n)"

  private val attributeValueMap = Map(
    attributes map { attr => attr.unapply }: _*
  )

  def attributeValue(name: String) = attributeValueMap(name)
}

object User {
  def apply(config: Config) = new User(
    config.getString("name"),
    config.getString("reportName"),
    (config.getConfigList("attributes").asScala map (Attribute(_))).toList
  )

  def isAdmin(username: String) = {
    "Admin".equals(username)
  }
}