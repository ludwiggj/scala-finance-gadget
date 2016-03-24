package models.org.ludwiggj.finance.persistence

import models.org.ludwiggj.finance.Transaction
import models.org.ludwiggj.finance.domain.Price
import scala.language.implicitConversions

package object database {
  // TODO - Type of second String should be FundName
  type TransactionMapKey = (String, String)
  type TransactionMap = Map[TransactionMapKey, (Seq[Transaction], Price)]
}