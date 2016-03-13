package models.org.ludwiggj.finance.experiment.app.stubbed

import org.specs2.mutable.Specification

class DateServiceCompStubbedSpec extends Specification with DateServiceComp with StubPlayAppComp {

  "date service" should {

    "parse with a stubbed app" in  {

      dateUtil.parse("2013-05-14 23:30:00") mustEqual 1368570600000L

    }

    "format with a stubbed app" in {

       dateUtil.format(1368570600000L) mustEqual "2013-05-14 23:30:00"
    }
  }
}