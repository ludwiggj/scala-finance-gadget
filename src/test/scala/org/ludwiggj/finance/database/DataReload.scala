package org.ludwiggj.finance.database

import org.ludwiggj.finance._
import org.ludwiggj.finance.persistence.{DatabasePersister, FileHoldingFactory}

object DataReload extends App {
  private val holdings = new FileHoldingFactory(s"$reportHome/holdings_15_01_24_Graeme.txt").getHoldings()
  new DatabasePersister().persist("Graeme", holdings)
}