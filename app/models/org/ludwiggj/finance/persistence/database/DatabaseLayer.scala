package models.org.ludwiggj.finance.persistence.database

import slick.driver.JdbcProfile

class DatabaseLayer(val profile: JdbcProfile) extends Tables with Profile