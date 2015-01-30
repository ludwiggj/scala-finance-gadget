package org.ludwiggj.finance.database

import java.io.{FilenameFilter, File}

import org.ludwiggj.finance._
import org.ludwiggj.finance.persistence.{DatabasePersister, FileHoldingFactory}

object DataReload extends App {
  val holdingPattern = """holdings.*_([a-zA-Z]+)\.txt""".r

  val isAHoldingsFile = new FilenameFilter {
    override def accept(dir: File, name: String) = {
      name match {
        case holdingPattern(userName) => true
        case _ => false
      }
    }
  }

  for (
    aFile <- new File(reportHome).listFiles(isAHoldingsFile)
  ) {
    val fileName = aFile.getName
    val holdingPattern(userName) = fileName
    val holdings = new FileHoldingFactory(s"$reportHome/$fileName").getHoldings()

    println(s"Persisting holdings for user $userName, file $fileName")
    new DatabasePersister().persist(userName, holdings)
  }
}