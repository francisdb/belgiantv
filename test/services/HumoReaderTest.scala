package services

import models.Channel
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import org.joda.time.DateMidnight
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent._
import scala.concurrent.duration._

class HumoReaderTest extends Specification{
  
  "the humo reader" should {
    "return data" in running(GuiceApplicationBuilder().build()) {
      // if their cache is not warmed up and we get a 504 then we retry after 1 min
      val list = Await.result(HumoReader.fetchDay(new DateMidnight(), Channel.channelFilter), 2.minutes)
      //list.foreach(println)
      list.size must be > 0
    }
  }
  
}