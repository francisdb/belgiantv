package services

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import org.joda.time.DateMidnight

import scala.concurrent._
import scala.concurrent.duration._
import models.Channel

class YeloReaderTest extends Specification{
  "the yelo reader" should {
    "return data" in {
      running(FakeApplication()) {
        val list = Await.result(YeloReader.fetchDay(new DateMidnight, Channel.channelFilter), 30.seconds)
        //list.map(_.channel).distinct.foreach(println)
        //ist.groupBy(_.channel).foreach(g => println(g._1, g._2.mkString("\n\t")))
        list.size must be > 20
      }
    }
  }
}