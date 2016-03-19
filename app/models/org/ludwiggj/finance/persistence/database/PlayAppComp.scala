package models.org.ludwiggj.finance.persistence.database

import com.typesafe.config.ConfigFactory
import play.api.Play

trait PlayAppComp {

  lazy val playApp = new PlayApp

  class PlayApp {

    implicit def app = Play.current

    def isProd = Play.isProd
    def configuration = Play.configuration
    def getFile(path: String) = Play.getExistingFile(path).getOrElse(
      throw new RuntimeException(s"File not found at $path.")
    )
    def getConfig(path: String) = ConfigFactory.parseFile(getFile(path))
  }
}