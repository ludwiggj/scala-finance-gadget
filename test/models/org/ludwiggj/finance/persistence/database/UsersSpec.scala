package models.org.ludwiggj.finance.persistence.database

import Tables.UsersRow
import UsersDatabase.stringToUsersRow
import org.scalatest.{BeforeAndAfter, DoNotDiscover}
import org.scalatestplus.play.{ConfiguredApp, PlaySpec}

@DoNotDiscover
class UsersSpec extends PlaySpec with DatabaseHelpers with ConfiguredApp with BeforeAndAfter {

  before {
    DatabaseCleaner.recreateDb()
  }

  "get" should {
    "return empty if user is not present" in {
      EmptySchema.loadData()

      UsersDatabase().get("Burt Bacharach") must equal(None)
    }

    "return existing user row if it is present" in {
      SingleUser.loadData()

      UsersDatabase().get(fatherTedUserName) mustBe Some(
        UsersRow(fatherTedUserId, fatherTedUserName)
      )
    }
  }

  "getOrInsert" should {
    "insert user if it is not present" in {
      EmptySchema.loadData()

      UsersDatabase().getOrInsert("bob") must be > 0L
    }

    "return existing user id if user is present" in {
      SingleUser.loadData()

      UsersDatabase().getOrInsert(fatherTedUserName) must equal(fatherTedUserId)
    }
  }
}