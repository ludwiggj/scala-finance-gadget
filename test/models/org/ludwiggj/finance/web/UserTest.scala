package models.org.ludwiggj.finance.web

import com.typesafe.config.Config
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
      (config.getString _).expects("reportName").returning("reportName")
      (config.getString _).expects("username").returning("username")
      (config.getString _).expects("password").returning("password")
      (config.getString _).expects("accountId").returning("accountId")
      (config.getConfigList _).expects("attributes").returning(attributes)
      (attribute1.getString _).expects("name").returning("attr1")
      (attribute1.getString _).expects("value").returning("value1")
      (attribute2.getString _).expects("name").returning("attr2")
      (attribute2.getString _).expects("value").returning("value2")
    }

    val user = User(config)

    user.name should equal ("userName")
    user.reportName should equal ("reportName")
    user.username should equal ("username")
    user.password should equal ("password")
    user.accountId should equal ("accountId")
    user.attributeValue("attr1") should equal ("value1")
    user.attributeValue("attr2") should equal ("value2")
  }
}