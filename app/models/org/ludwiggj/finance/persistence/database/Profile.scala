package models.org.ludwiggj.finance.persistence.database

import slick.driver.JdbcProfile

trait Profile {
  val profile: JdbcProfile
}