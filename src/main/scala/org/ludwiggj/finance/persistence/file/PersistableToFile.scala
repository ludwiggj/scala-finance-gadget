package org.ludwiggj.finance.persistence.file

trait PersistableToFile {
  def toFileFormat(): String
}
