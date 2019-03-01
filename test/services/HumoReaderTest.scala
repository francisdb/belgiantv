package services

import java.time.LocalDate

import global.Globals
import helper.WithWsClient
import models.Channel
import org.specs2.mutable._
import org.specs2.concurrent.ExecutionEnv

import scala.concurrent.duration._

class HumoReaderTest(implicit ee: ExecutionEnv) extends Specification with WithWsClient {
  
  "the humo reader" should {

    "return data" in {
      val humo = new HumoReader(ws)
      // if their cache is not warmed up and we get a 504 then we retry after 1 min
      val list = humo.fetchDay(LocalDate.now(Globals.timezone), Channel.channelFilter)
      //list.foreach(_.foreach(println))
      list.map(_.size) must be_>(0).awaitFor(2.minutes)
    }
  }
  
}
