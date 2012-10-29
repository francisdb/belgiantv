package services

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import java.util.concurrent.TimeUnit
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.joda.time.DateMidnight

@RunWith(classOf[JUnitRunner])
class YeloReaderTest extends Specification{
  "the yelo reader" should {
    "return data" in {
      running(FakeApplication()) {
        val result = YeloReader.fetchDay(new DateMidnight)
        val list = result.await(30, TimeUnit.SECONDS).get
        //println(list)
        list.size must be > 20
      }
    }
  }
}