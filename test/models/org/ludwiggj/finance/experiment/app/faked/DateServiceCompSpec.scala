package models.org.ludwiggj.finance.experiment.app.faked

import org.specs2.mutable.Specification
import play.api.test.WithApplication

class DateServiceCompSpec extends Specification with DateServiceComp {

  sequential

  "date service" should {

    "parse with a fake app" in new WithApplication {

      dateUtil.parse("2013-05-14 23:30:00") mustEqual 1368570600000L

    }

    "format with a fake app" in new WithApplication {

      dateUtil.format(1368570600000L) mustEqual "2013-05-14 23:30:00"
    }
  }
}