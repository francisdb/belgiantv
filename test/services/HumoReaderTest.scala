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

class HumoReaderTest extends Specification with NoTimeConversions {
  
  "the humo reader" should {
    "return data" in {
      running(FakeApplication()) {
        // if their cache is not warmed up and we get a 504 then we retry after 1 min
        val list = Await.result(HumoReader.fetchDay(new DateMidnight), 2.minutes)
        //list.foreach(println)
        list.size must be > 0
      }
    }
  }
  
}