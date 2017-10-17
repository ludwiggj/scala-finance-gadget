package models.org.ludwiggj.finance.persistence.database

import slick.jdbc.JdbcProfile

trait Profile {
  val profile: JdbcProfile
}