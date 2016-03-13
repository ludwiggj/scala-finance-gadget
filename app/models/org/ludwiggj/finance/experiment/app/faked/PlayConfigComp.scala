package models.org.ludwiggj.finance.experiment.app.faked

import play.api.Play

trait PlayConfigComp {

  lazy val playConfig = new PlayConfig

  class PlayConfig {

    import play.api.Play.current
    private val underlying = Play.configuration.underlying

    val dateFormat = underlying.getString("date.format")
  }
}