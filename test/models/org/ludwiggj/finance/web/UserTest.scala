package models.org.ludwiggj.finance.web

import com.typesafe.config.Config
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSuite, Matchers}

class UserTest extends FunSuite with MockFactory with Matchers {

  test("Create user from Config") {
    val config = mock[Config]

    inAnyOrder {
      (config.getString _).expects("name").returning("userName")
      (config.getString _).expects("reportName").returning("reportName")
      (config.getString _).expects("username").returning("username")
      (config.getString _).expects("password").returning("password")
      (config.getString _).expects("accountId").returning("accountId")
    }

    val user = User(config)

    user.name should equal ("userName")
    user.reportName should equal ("reportName")
    user.username should equal ("username")
    user.password should equal ("password")
    user.accountId should equal ("accountId")
  }
}