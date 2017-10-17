package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.FundName
import models.org.ludwiggj.finance.persistence.database.PKs.PK
import models.org.ludwiggj.finance.persistence.database.fixtures.SingleFund
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.language.postfixOps

class FundSpec extends PlaySpec with HasDatabaseConfigProvider[JdbcProfile] with GuiceOneAppPerSuite with BeforeAndAfter {

  before {
    // See https://stackoverflow.com/questions/31884182/play-2-4-2-play-slick-1-0-0-how-do-i-apply-database-evolutions-to-a-slick-man
    val databaseApi = app.injector.instanceOf[DBApi]
    val defaultDatabase = databaseApi.database("default")
    cleanupEvolutions(defaultDatabase)
    applyEvolutions(defaultDatabase)
  }

  lazy val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]

  private val nonExistentFund = FundName("fundThatIsNotPresent")

  "the Fund database API" should {
    "provide a get method," which {
      "returns empty if the fund is not present" in new DatabaseLayer(dbConfig) {
        exec(Funds.get(nonExistentFund)) must equal(None)
      }

      "returns the existing fund row if it is present" in new DatabaseLayer(dbConfig) with SingleFund {

        exec(Funds.get(fundName)) mustBe Some(
          FundRow(existingFundId, fundName)
        )
      }
    }

    "provide a getOrInsert method," which {
      "inserts the fund if it is not present" in new DatabaseLayer(dbConfig) {
        exec(Funds.getOrInsert(nonExistentFund)) must be > PK[FundTable](0L)
      }

      "returns the existing fund id if the fund is present" in new DatabaseLayer(dbConfig) with SingleFund {
        exec(Funds.getOrInsert(fundName)) must equal(existingFundId)
      }
    }
  }
}