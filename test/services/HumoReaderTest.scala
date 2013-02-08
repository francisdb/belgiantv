package services

import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import org.specs2.time.NoTimeConversions

import play.api.test._
import play.api.test.Helpers._

import org.junit.runner.RunWith
import org.joda.time.DateMidnight

import scala.concurrent._
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class HumoReaderTest extends Specification with NoTimeConversions {
  
  "the huno reader" should {
    "return data" in {
      running(FakeApplication()) {
        val list = Await.result(HumoReader.fetchDay(new DateMidnight), 30 seconds)
        println(list)
        list.size must be > 20
      }
    }
  }
  
}