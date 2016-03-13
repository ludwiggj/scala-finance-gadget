package models.org.ludwiggj.finance.experiment.app.faked

import java.text.SimpleDateFormat


trait DateServiceComp extends PlayConfigComp {

  lazy val dateUtil = new DateService

  class DateService {

    def parse(date: String) = dateFormat.parse(date).getTime
    def format(timeInMillis: Long) = dateFormat.format(timeInMillis)

    private def dateFormat = new SimpleDateFormat(playConfig.dateFormat)
  }
}
