package models.org.ludwiggj.finance.experiment.app.stubbed

trait PlayConfigComp extends PlayAppComp {

  lazy val playConfig = new PlayConfig

  class PlayConfig {

    private val underlying = playApp.configuration.underlying

    val dateFormat = underlying.getString("date.format")
  }
}