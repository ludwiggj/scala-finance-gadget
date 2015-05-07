package org.ludwiggj.finance.persistence.file

import java.io.File

import org.ludwiggj.finance.domain.separator

import scala.io.Source._

trait FileFinanceRowParser {
  val fileName: String

  def getLines() = {
    val resource =
      if (fileName.startsWith("/")) getClass.getResource(s"$fileName")
      else new File(fileName).toURI().toURL()

    val source = fromURL(resource)

    val lines = (for (l <- source.getLines) yield l.split(separator)).toList

    source.close()

    lines
  }
}