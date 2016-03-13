package models.org.ludwiggj.finance.experiment.app.stubbed

import java.io.File

import play.api.Configuration

trait StubPlayAppComp extends PlayAppComp {

  override lazy val playApp = new StubPlayApp

  class StubPlayApp extends PlayApp {

    override def isProd = false
    override def configuration = Configuration(getConfig("conf/test.conf"))
    override def getFile(path: String) = new File(path)
  }
}