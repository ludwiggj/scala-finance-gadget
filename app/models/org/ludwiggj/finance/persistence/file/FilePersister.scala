package models.org.ludwiggj.finance.persistence.file

import java.io.PrintWriter

class FilePersister private(private val fileName: String) {
  def write[T <: PersistableToFile](persistables: List[T]) = {
    val out = new PrintWriter(fileName)
    for (persistable <- persistables) out.println(persistable.toFileFormat)
    out.close
    println(s"Entries persisted to file $fileName")
  }
}

object FilePersister {
  def apply(fileName: String) = new FilePersister(fileName)
}