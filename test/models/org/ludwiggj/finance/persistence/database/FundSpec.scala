package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.FundName
import models.org.ludwiggj.finance.persistence.database.PKs.PK
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

class FundSpec extends PlaySpec with OneAppPerSuite with HasDatabaseConfigProvider[JdbcProfile] with BeforeAndAfter {

  private val nonExistentFund = FundName("fundThatIsNotPresent")

  lazy val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]

  before {
    val dbAPI = app.injector.instanceOf[DBApi]
    val defaultDatabase = dbAPI.database("default")
    Evolutions.cleanupEvolutions(defaultDatabase)
    Evolutions.applyEvolutions(defaultDatabase)
  }

  val databaseLayer = new DatabaseLayer(app.injector.instanceOf[DatabaseConfigProvider].get)

  import databaseLayer._

  object SingleFund {
    val fundName = FundName("Capitalists Dream")

    def insert(): PK[FundTable] = {
      exec(Funds.insert(fundName))
    }
  }

  "get" must {
    "return empty if fund is not present" in {
      exec(Funds.get(nonExistentFund)) must equal(None)
    }

    "return existing fund row if it is present" in {
      val fundId = SingleFund.insert()

      exec(Funds.get(SingleFund.fundName)) mustBe Some(
        FundRow(fundId, SingleFund.fundName)
      )
    }
  }

  "getOrInsert" should {
    "insert fund if it is not present" in {
      exec(Funds.getOrInsert(nonExistentFund)) must be > PK[FundTable](0L)
    }

    "return existing fund id if fund is present" in {
      val fundId = SingleFund.insert()

      exec(Funds.getOrInsert(SingleFund.fundName)) must equal(fundId)
    }
  }
}