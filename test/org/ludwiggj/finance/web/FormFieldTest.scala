package org.ludwiggj.finance.web

import com.typesafe.config.Config
import models.org.ludwiggj.finance.web.FormField
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSuite, Matchers}

class FormFieldTest extends FunSuite with MockFactory with Matchers {

  test("Create form field from Config") {
    val config = mock[Config]
    val name = "fieldName"
    val htmlName = "fieldHtmlName"

    inAnyOrder {
      (config.getString _).expects("name").returning(name)
      (config.getString _).expects("htmlName").returning(htmlName)
    }

    FormField(config).toString should equal ("FormField (name: fieldName htmlName: fieldHtmlName)")
  }
}