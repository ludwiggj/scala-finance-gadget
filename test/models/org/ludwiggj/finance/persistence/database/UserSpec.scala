package models.org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.domain.User
import models.org.ludwiggj.finance.persistence.database.PKs.PK
import org.scalatest.{BeforeAndAfter, DoNotDiscover}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}
import models.org.ludwiggj.finance.persistence.database.Tables.{UserTable, UserRow}

@DoNotDiscover
class UserSpec extends PlaySpec with DatabaseHelpers with ConfiguredApp with BeforeAndAfter {

  before {
    TestDatabase.deleteAllData()
  }

  "get" should {
    "return empty if user is not present" in {
      EmptySchema.loadData()

      User.get("Burt Bacharach") must equal(None)
    }

    "return existing user row if it is present" in {
      SingleUser.loadData()

      User.get(SingleUser.username) mustBe Some(
        UserRow(SingleUser.userId, SingleUser.username, SingleUser.password)
      )
    }
  }

  "getOrInsert" should {
    "insert user if it is not present" in {
      EmptySchema.loadData()

      User.getOrInsert("bob") must be > PK[UserTable](0L)
    }

    "return existing user id if user is present" in {
      SingleUser.loadData()

      User.getOrInsert(SingleUser.username) must equal(SingleUser.userId)
    }
  }

  "authenticate" should {
    "return 0 if user is not present" in {
      EmptySchema.loadData()

      User.authenticate("dummy user", "blah") must equal(0)
    }

    "return 1 if user is present and password does not match" in {
      SingleUser.loadData()

      User.authenticate(SingleUser.username, "blah") must equal(0)
    }

    "return 1 if user is present and password matches" in {
      SingleUser.loadData()

      User.authenticate(SingleUser.username, SingleUser.password.get) must equal(1)
    }
  }
}