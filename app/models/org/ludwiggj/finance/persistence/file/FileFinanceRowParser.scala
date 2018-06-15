package models.org.ludwiggj.finance.persistence.file

import java.io.File
import scala.io.Source._
import models.org.ludwiggj.finance.domain.separator

trait FileFinanceRowParser {
  val fileName: String

  def fetchLines(): List[Array[String]] = {
    // TODO - Alter, see where this breaks?
    val resource =
      if (fileName.startsWith("/")) getClass.getResource(s"$fileName")
      else new File(fileName).toURI.toURL

    val source = fromURL(resource)

    val lines = (for (l <- source.getLines) yield l.split(separator)).toList

    source.close()

    lines
  }
}