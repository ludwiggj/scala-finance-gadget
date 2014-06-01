package org.ludwiggj.finance.persistence

trait Persistable {
  def toFileFormat(): String
}
