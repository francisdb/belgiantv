package services

import helper.WithWS
import org.specs2.mutable._
import org.joda.time.DateMidnight

import scala.concurrent._
import scala.concurrent.duration._
import models.Channel

class YeloReaderTest extends Specification{

  "the yelo reader" should {

    "return data" in new WithWS{
      val yelo = new YeloReader(ws)
      val list = Await.result(yelo.fetchDay(new DateMidnight, Channel.channelFilter), 30.seconds)
      //list.map(_.channel).distinct.foreach(println)
//        list.groupBy(_.channel).foreach{case (channel, item) =>
//          println(s"$channel -> ${item.mkString("\n\t")}")
//        }
      list.size must be > 20
    }
  }
}