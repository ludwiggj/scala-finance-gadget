package models.org.ludwiggj.finance.persistence.database

import org.scalatest.{BeforeAndAfter, Suites}
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.FakeApplication
import play.api.{Configuration, Environment, Play}

// This is the "master" suite
class MasterSuite {
//  extends Suites(
//  new FundSpec
//  new UserSpec,
//  new PortfolioListSpec,
//  new PriceSpec,
//  new TransactionSpec
//) with OneAppPerSuite with BeforeAndAfter {
//
//  val additionalConfig = Configuration.load(Environment.simple(), Map("config.resource" -> "test.conf")).underlying
//
//  def getConfig = Map(
//    "db_name" -> additionalConfig.getString("db_name"),
//    "db.financeTest.url" -> additionalConfig.getString("db.financeTest.url"),
//    "db.financeTest.username" -> additionalConfig.getString("db.financeTest.username"),
//    "db.financeTest.password" -> additionalConfig.getString("db.financeTest.password")
//  )
//
//   Override app if you need a FakeApplication with other than non-default parameters.
//  implicit override lazy val app: FakeApplication =
//    FakeApplication(additionalConfiguration = getConfig)
//
//  Play.start(app)
}