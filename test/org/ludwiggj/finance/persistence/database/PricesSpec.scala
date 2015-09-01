package org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.FinanceDate
import models.org.ludwiggj.finance.persistence.database.Tables.PricesRow
import models.org.ludwiggj.finance.persistence.database.{PricesDatabase}
import org.specs2.mutable.Specification

class PricesSpec extends Specification with DatabaseHelpers {

  "Prices" should {
    "all be fetchable" in TwoPrices {
      PricesDatabase().get() must containTheSameElementsAs(
        List(
          PricesRow(1L, FinanceDate("20/05/2014").asSqlDate, 1.12),
          PricesRow(2L, FinanceDate("20/05/2014").asSqlDate, 2.79)
        ))
    }

    "be unchanged if attempt to add price for same date" in TwoPrices {
      val pricesDatabase = PricesDatabase()
      pricesDatabase.insert(PricesRow(1L, FinanceDate("20/05/2014").asSqlDate, 2.12))
      pricesDatabase.get() must containTheSameElementsAs(
        List(
          PricesRow(1L, FinanceDate("20/05/2014").asSqlDate, 1.12),
          PricesRow(2L, FinanceDate("20/05/2014").asSqlDate, 2.79)
        ))
    }

    "be increase by one if add new unique price" in TwoPrices {
      val pricesDatabase = PricesDatabase()
      pricesDatabase.insert(PricesRow(1L, FinanceDate("21/05/2014").asSqlDate, 2.12))
      pricesDatabase.get() must containTheSameElementsAs(
        List(
          PricesRow(1L, FinanceDate("20/05/2014").asSqlDate, 1.12),
          PricesRow(2L, FinanceDate("20/05/2014").asSqlDate, 2.79),
          PricesRow(1L, FinanceDate("21/05/2014").asSqlDate, 2.12)
        ))
    }
  }
}