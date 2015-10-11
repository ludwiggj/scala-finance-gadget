package models.org.ludwiggj.finance.persistence.database

import Tables.UsersRow
import UsersDatabase.stringToUsersRow
import org.specs2.mutable.Specification

class UsersSpec extends Specification with DatabaseHelpers {

  "get" should {
    "return empty if user is not present" in EmptySchema {
      UsersDatabase().get("Burt Bacharach") must beEqualTo(None)
    }

    "return existing user row if it is present" in SingleUser {
      UsersDatabase().get(fatherTedUserName) must beSome(
        UsersRow(fatherTedUserId, fatherTedUserName)
      )
    }
  }

  "getOrInsert" should {
    "insert user if it is not present" in EmptySchema {
      UsersDatabase().getOrInsert("bob") must be_>(0L)
    }

    "return existing user id if user is present" in SingleUser {
      UsersDatabase().getOrInsert(fatherTedUserName) must beEqualTo(fatherTedUserId)
    }
  }
}