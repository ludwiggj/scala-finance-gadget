package org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.{Price, FinanceDate}
import models.org.ludwiggj.finance.persistence.database.{PricesDatabase, UsersDatabase, FundsDatabase}
import models.org.ludwiggj.finance.persistence.database.UsersDatabase.usersRowWrapper
import models.org.ludwiggj.finance.persistence.database.FundsDatabase.fundsRowWrapper
import models.org.ludwiggj.finance.persistence.database.PricesDatabase.pricesRowWrapper
import org.specs2.execute.AsResult
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Around
import org.specs2.specification.mutable.SpecificationFeatures
import play.api.Play.current
import play.api.db.DB
import play.api.test.FakeApplication
import play.api.test.Helpers._

import scala.io.Source
import scala.slick.driver.MySQLDriver.simple._


trait DatabaseHelpers {
  this: SpecificationFeatures =>

  var fatherTedUserId = 0L
  var capitalistsDreamFundId = 0L

  trait Schema extends Around {

    def sqlFiles = List("1.sql", "2.sql", "3.sql", "4.sql", "5.sql")

    def ddls = for {
      sqlFile <- sqlFiles
      evolutionContent = Source.fromFile(s"conf/evolutions/finance/$sqlFile").getLines.mkString("\n")
      splitEvolutionContent = evolutionContent.split("# --- !Ups")
      upsDowns = splitEvolutionContent(1).split("# --- !Downs")
    } yield (upsDowns(1), upsDowns(0))

    def dropDdls = (ddls map {
      _._1
    }).reverse

    def createDdls = ddls map {
      _._2
    }

    def dropCreateDb() = {
      DB.withConnection("finance") { implicit connection =>

        for (ddl <- dropDdls ++ createDdls) {
          connection.createStatement.execute(ddl)
        }
      }
    }

    def around[T: AsResult](test: => T) = {

      def getConfig = Map(
        "db.finance.driver" -> "com.mysql.jdbc.Driver",
        "db.finance.url" -> "jdbc:mysql://localhost:3306/financeTest",
        "db.finance.user" -> "financeTest",
        "db.finance.password" -> "geckoTest",
        "db.finance.maxConnectionAge" -> 0,
        "db.finance.disableConnectionTracking" -> true
      )

      def fakeApp[T](block: => T): T = {
        val fakeApplication = FakeApplication(additionalConfiguration = getConfig)

        running(fakeApplication) {
          def db = Database.forDataSource(DB.getDataSource("finance")(fakeApplication))
          db.withSession { implicit s: Session => block }
        }
      }

      fakeApp {
        dropCreateDb()
        test.asInstanceOf[MatchResult[T]].toResult
      }
    }
  }

  object EmptySchema extends Schema {
  }

  object SingleUser extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      fatherTedUserId = UsersDatabase().getOrInsert("father ted")
      test
    }
  }

  object SingleFund extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      capitalistsDreamFundId = FundsDatabase().getOrInsert("Capitalists Dream")
      test
    }
  }

  object TwoPrices extends Schema {
    override def around[T: AsResult](test: => T) = super.around {

      val holding1Id = FundsDatabase().getOrInsert("holding1")
      val holding2Id = FundsDatabase().getOrInsert("holding2")

      PricesDatabase().insert((holding1Id, Price("holding1", FinanceDate("20/05/2014"), 1.12)))
      PricesDatabase().insert((holding2Id, Price("holding2", FinanceDate("20/05/2014"), 2.79)))
      test
    }
  }
}