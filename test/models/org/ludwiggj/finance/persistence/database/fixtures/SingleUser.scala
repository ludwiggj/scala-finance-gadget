package models.org.ludwiggj.finance.persistence.database.fixtures

import models.org.ludwiggj.finance.persistence.database.DatabaseLayer

trait SingleUser {
  this: DatabaseLayer =>

  val username = "Father_Ted"
  val password = Some("Penitent_Man")

  val userId = exec(Users.insert(username, password))
}