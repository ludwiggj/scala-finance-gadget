package models.org.ludwiggj.finance.persistence.database

import Tables.UsersRow
import models.org.ludwiggj.finance.domain.User
import models.org.ludwiggj.finance.domain.User.stringToUsersRow
import org.scalatest.{BeforeAndAfter, DoNotDiscover}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}

@DoNotDiscover
class UserSpec extends PlaySpec with DatabaseHelpers with ConfiguredApp with BeforeAndAfter {

  before {
    DatabaseCleaner.recreateDb()
  }

  "get" should {
    "return empty if user is not present" in {
      EmptySchema.loadData()

      User.get("Burt Bacharach") must equal(None)
    }

    "return existing user row if it is present" in {
      SingleUser.loadData()

      User.get(fatherTedUserName) mustBe Some(
        UsersRow(fatherTedUserId, fatherTedUserName)
      )
    }
  }

  "getOrInsert" should {
    "insert user if it is not present" in {
      EmptySchema.loadData()

      User.getOrInsert("bob") must be > 0L
    }

    "return existing user id if user is present" in {
      SingleUser.loadData()

      User.getOrInsert(fatherTedUserName) must equal(fatherTedUserId)
    }
  }
}