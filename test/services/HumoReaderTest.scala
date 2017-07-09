package services

import helper.WithWsClient
import models.Channel
import org.specs2.mutable._
import org.joda.time.DateMidnight

import scala.concurrent._
import scala.concurrent.duration._

class HumoReaderTest extends Specification with WithWsClient {
  
  "the humo reader" should {

    "return data" in {
      val humo = new HumoReader(ws)
      // if their cache is not warmed up and we get a 504 then we retry after 1 min
      val list = Await.result(humo.fetchDay(new DateMidnight(), Channel.channelFilter), 2.minutes)
      //list.foreach(println)
      list.size must be > 0
    }
  }
  
}