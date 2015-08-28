package models.org.ludwiggj.finance.persistence

import java.sql.Date
import models.org.ludwiggj.finance.persistence.database.Tables.UsersRow
import scala.language.implicitConversions

package object database {
  type TransactionTuple = (String, Date, String, BigDecimal, BigDecimal, Date, BigDecimal, BigDecimal)

  implicit def usersRowWrapper(name: String) = UsersRow(0L, name)
}