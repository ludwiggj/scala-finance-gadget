package org.ludwiggj.finance.web

import com.typesafe.config.Config
import models.org.ludwiggj.finance.web.{User, User$}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSuite, Matchers}

import scala.collection.JavaConverters._

class UserTest extends FunSuite with MockFactory with Matchers {

  test("Create user from Config") {
    val config = mock[Config]
    val attribute1 = mock[Config]
    val attribute2 = mock[Config]
    val attributes = List(attribute1, attribute2).asJava

    inAnyOrder {
      (config.getString _).expects("name").returning("userName")
      (config.getConfigList _).expects("attributes").returning(attributes)
      (attribute1.getString _).expects("name").returning("attr1")
      (attribute1.getString _).expects("value").returning("value1")
      (attribute2.getString _).expects("name").returning("attr2")
      (attribute2.getString _).expects("value").returning("value2")
    }

    val user = User(config)

    user.name should equal ("userName")
    user.attributeValue("attr1") should equal ("value1")
    user.attributeValue("attr2") should equal ("value2")
  }
}