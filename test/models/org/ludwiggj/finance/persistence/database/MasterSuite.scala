package models.org.ludwiggj.finance.persistence.database

import org.scalatest.Suites
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.FakeApplication

// This is the "master" suite
class MasterSuite extends Suites(
  new FundSpec,
  new UserSpec,
  new PortfolioListSpec,
  new PriceSpec,
  new TransactionSpec
) with OneAppPerSuite {

  def getConfig = Map(
    "db.finance.driver" -> "com.mysql.jdbc.Driver",
    "db.finance.url" -> "jdbc:mysql://localhost:3306/financeTest",
    "db.finance.user" -> "financeTest",
    "db.finance.password" -> "geckoTest",
    "db.finance.maxConnectionAge" -> 0,
    "db.finance.disableConnectionTracking" -> true
  )

  // Override app if you need a FakeApplication with other than non-default parameters.
  implicit override lazy val app: FakeApplication =
    FakeApplication(additionalConfiguration = getConfig)
}
