package org.ludwiggj.finance.persistence.database

import models.org.ludwiggj.finance.persistence.database.UserDatabase
import org.specs2.mutable.Specification

class UserSpec extends Specification with DatabaseHelpers {
  "User" should {
    "be able to be inserted" in EmptySchema {
      val user = "bob"
      val userId = UserDatabase().getOrInsert(user)
      userId must be_>(0L)
    }

    "be able to be found" in SingleUser {
      val userId = UserDatabase().getOrInsert("father ted")
      userId must beEqualTo(fatherTedUserId)
    }
  }
}