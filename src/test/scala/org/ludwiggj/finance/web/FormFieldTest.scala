package org.ludwiggj.finance.web

import org.scalatest.{Matchers, FunSuite}
import org.scalamock.scalatest.MockFactory
import com.typesafe.config.Config

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