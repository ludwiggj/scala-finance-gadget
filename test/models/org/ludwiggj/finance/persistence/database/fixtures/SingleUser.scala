package models.org.ludwiggj.finance.persistence.database.fixtures

import models.org.ludwiggj.finance.persistence.database.DatabaseLayer
import models.org.ludwiggj.finance.persistence.database.PKs.PK

trait SingleUser {
  this: DatabaseLayer =>

  val username = "Father_Ted"
  val password = Some("Penitent_Man")

  val userId: PK[UserTable] = exec(Users.insert(username, password))
}