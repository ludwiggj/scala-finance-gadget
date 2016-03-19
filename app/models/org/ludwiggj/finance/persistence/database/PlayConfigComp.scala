package models.org.ludwiggj.finance.persistence.database

trait PlayConfigComp extends PlayAppComp {

  lazy val playConfig = new PlayConfig

  class PlayConfig {
    val underlying = playApp.configuration.underlying
  }
}