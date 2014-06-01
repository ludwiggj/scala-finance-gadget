package org.ludwiggj.finance.persistence

import java.io.PrintWriter

class Persister(private val fileName: String) {
  def write[T <: Persistable](persistables: List[T]) = {
    val out = new PrintWriter(fileName)
    for (persistable <- persistables) out.println(persistable.toFileFormat)
    out.close
  }
}

object Persister {
  def apply(fileName: String) = new Persister(fileName)
}