package models.org.ludwiggj.finance.persistence.database

import javax.inject.Inject

import models.org.ludwiggj.finance.domain.FundName
import models.org.ludwiggj.finance.persistence.database.PKs.PK
import org.scalatest.{BeforeAndAfter, DoNotDiscover}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps

@DoNotDiscover
class FundSpec extends PlaySpec with ConfiguredApp with BeforeAndAfter {

  before {
    TestDatabase.recreateSchema()

  }

  private val nonExistentFund = FundName("fundThatIsNotPresent")

  lazy val dbConfig = DatabaseConfigProvider.get[JdbcProfile]("financeTest")(Play.current)
  val db = dbConfig.db
  val databaseLayer = new DatabaseLayer(dbConfig.driver)
  import databaseLayer._
  import profile.api._

  def exec[T](action: DBIO[T]): T = Await.result(db.run(action), 2 seconds)

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