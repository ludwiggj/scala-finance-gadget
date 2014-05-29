package org.ludwiggj.finance.web

import org.scalatest.{Matchers, FunSuite}
import org.scalamock.scalatest.MockFactory
import com.typesafe.config.Config
import scala.collection.JavaConverters._

class AccountTest extends FunSuite with MockFactory with Matchers {

  test("Create account from Config") {
    val config = mock[Config]
    val attribute1 = mock[Config]
    val attribute2 = mock[Config]
    val attributes = List(attribute1, attribute2).asJava

    inAnyOrder {
      (config.getString _).expects("name").returning("accountName")
      (config.getConfigList _).expects("attributes").returning(attributes)
      (attribute1.getString _).expects("name").returning("attr1")
      (attribute1.getString _).expects("value").returning("value1")
      (attribute2.getString _).expects("name").returning("attr2")
      (attribute2.getString _).expects("value").returning("value2")
    }

    val acc = Account(config)

    acc.name should equal ("accountName")
    acc.attributeValue("attr1") should equal ("value1")
    acc.attributeValue("attr2") should equal ("value2")
  }
}