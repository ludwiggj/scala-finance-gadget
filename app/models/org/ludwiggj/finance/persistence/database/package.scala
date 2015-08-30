package models.org.ludwiggj.finance.persistence

import java.sql.Date
import scala.language.implicitConversions

package object database {
  type TransactionTuple = (String, Date, String, BigDecimal, BigDecimal, Date, BigDecimal, BigDecimal)
}