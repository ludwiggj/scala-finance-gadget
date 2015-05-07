package org.ludwiggj.finance.persistence

import java.sql.Date

package object database {
  type TransactionTuple = (String, Date, String, BigDecimal, BigDecimal, Date, BigDecimal, BigDecimal)
}