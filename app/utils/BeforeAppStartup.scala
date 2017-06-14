package utils

import javax.inject._

import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Environment}


// NOTE: This (together with StartupModule, and play.modules.enabled entry added to application.conf),
// represents an action that is triggered AFTER the initial request is made to load a page of the play
// application, but BEFORE the application is loaded.

// Note that any attempt to run slick code against the database fails, as an implicit application is
// required to run it. Even if it is injected, the application is not running at this point.

// The best that can be done instead is to call the code via an action, at which point the application
// is running, as per
// https://stackoverflow.com/questions/31454857/playframework-2-4-globalsettings-onstart-deprecated.

@Singleton
class BeforeAppStartup @Inject()(applicationLifecycle: ApplicationLifecycle,
                                 config: Configuration,
                                 environment: Environment) {
  def hello(): Unit = println(">>>>>>>>> Hello <<<<<<<<<<<")

  hello()

  println("+++++" + config.getList("play.modules.enabled") + "+++++")

//  import play.api.db.DB
//  import scala.slick.driver.MySQLDriver.simple._
//  import models.org.ludwiggj.finance.persistence.database.Tables.{FundTable, PriceTable, TransactionTable, UserTable}
//  import play.api.inject.guice.GuiceApplicationBuilder

//  implicit val application = new GuiceApplicationBuilder(configuration = config).build()

//  def deleteAllRows() = {
//    lazy val db = Database.forDataSource(DB.getDataSource("finance"))
//    val users: TableQuery[UserTable] = TableQuery[UserTable]
//    val funds: TableQuery[FundTable] = TableQuery[FundTable]
//    val prices: TableQuery[PriceTable] = TableQuery[PriceTable]
//    val transactions: TableQuery[TransactionTable] = TableQuery[TransactionTable]
//
//
//    db.withSession {
//      implicit session =>
//        transactions.delete
//        prices.delete
//        funds.delete
//        users.delete
//    }
//  }
}