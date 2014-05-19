package org.ludwiggj.finance

import scala.io.Source
import org.filippodeluca.ssoup.SSoup._
import scala.language.postfixOps

object FinanceValuationScraper extends App {
  private val headerRow = 1

  // Retrieve from web site
  val page = loginAndParse("valuations")

  // Load from file...
//  val page = parsePageFromFile("resources/summary.txt")

  val holdingRows = page.select(s"table[id~=Holdings] tr") drop headerRow

  val holdings = for (holdingRow <- holdingRows) yield Holding(holdingRow.toString)

  holdings.foreach(h => println(h))

  println(s"Total holdings (map + sum): £${holdings map (h => h.value) sum}")

  println(s"Total holdings (fold): £${holdings.foldLeft(BigDecimal(0)) {
    (sum, h) => sum + h.value
  }}")
}