package org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.persistence.database.UsersDatabase
import models.org.ludwiggj.finance.persistence.database.UsersDatabase.usersRowWrapper
import org.specs2.mutable.Specification

class UsersSpec extends Specification with DatabaseHelpers {
  "User" should {
    "be able to be inserted" in EmptySchema {
      val user = "bob"
      val userId = UsersDatabase().getOrInsert(user)
      userId must be_>(0L)
    }

    "be able to be found" in SingleUser {
      val userId = UsersDatabase().getOrInsert("father ted")
      userId must beEqualTo(fatherTedUserId)
    }
  }
}