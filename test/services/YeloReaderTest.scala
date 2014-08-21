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
import models.Channel
import play.api.libs.ws.WS

//@RunWith(classOf[JUnitRunner])
class YeloReaderTest extends Specification with NoTimeConversions{
  "the yelo reader" should {
    "return data" in {
      running(FakeApplication()) {
        val list = Await.result(YeloReader.fetchDay(new DateMidnight, Channel.channelFilter), 30 seconds)
        //list.map(_.channel).distinct.foreach(println)
        l//ist.groupBy(_.channel).foreach(g => println(g._1, g._2.mkString("\n\t")))
        list.size must be > 20
      }
    }
  }
}