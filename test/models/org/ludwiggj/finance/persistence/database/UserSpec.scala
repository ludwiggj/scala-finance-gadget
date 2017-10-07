package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.persistence.database.PKs.PK
import models.org.ludwiggj.finance.persistence.database.fixtures.SingleUser
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions.{applyEvolutions, cleanupEvolutions}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

class UserSpec extends PlaySpec with HasDatabaseConfigProvider[JdbcProfile] with OneAppPerSuite with BeforeAndAfter {

  before {
    val databaseApi = app.injector.instanceOf[DBApi]
    val defaultDatabase = databaseApi.database("default")
    cleanupEvolutions(defaultDatabase)
    applyEvolutions(defaultDatabase)
  }

  lazy val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]

  "the User database API" should {
    "provide a get method," which {
      "returns empty if user is not present" in new DatabaseLayer(dbConfig) {
        exec(Users.get("Burt Bacharach")) must equal(None)
      }

      "returns the existing user row if it is present" in new DatabaseLayer(dbConfig) with SingleUser {
        exec(Users.get(username)) mustBe Some(
          UserRow(userId, username, password)
        )
      }
    }

    "provide a getOrInsert method," which {
      "inserts the user if it is not present" in new DatabaseLayer(dbConfig) with SingleUser {
        exec(Users.getOrInsert("bob")) must be > PK[UserTable](0L)
      }

      "returns the existing user id if the user is present" in new DatabaseLayer(dbConfig) with SingleUser {
        exec(Users.getOrInsert(username)) must equal(userId)
      }
    }

    "provide an authenticate method," which {
      "returns 0 if user is not present" in new DatabaseLayer(dbConfig) {
        exec(Users.authenticate("dummy user", "blah")) must equal(0)
      }

      "returns 1 if user is present and password does not match" in new DatabaseLayer(dbConfig) with SingleUser {
        exec(Users.authenticate(username, "blah")) must equal(0)
      }

      "return 1 if user is present and password matches" in new DatabaseLayer(dbConfig) with SingleUser {
        exec(Users.authenticate(username, password.get)) must equal(1)
      }
    }
  }
}