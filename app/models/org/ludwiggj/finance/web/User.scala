package models.org.ludwiggj.finance.web

import com.typesafe.config.Config

class User private(val name: String, val reportName: String, val username: String, val password: String,
                   val accountId: String) {
  override def toString =
    s"User (name: $name, reportName: $reportName, username: $username, password: $password, accountId: $accountId)"
}

object User {
  def apply(config: Config) = new User(
    config.getString("name"),
    config.getString("reportName"),
    config.getString("username"),
    config.getString("password"),
    config.getString("accountId")
  )

  def isAdmin(username: String): Boolean = {
    "Admin".equals(username)
  }
}