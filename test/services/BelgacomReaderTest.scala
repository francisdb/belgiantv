package services


import helper.WithWS

import org.joda.time.DateMidnight
import org.specs2.mutable._

import scala.concurrent._
import scala.concurrent.duration._

class BelgacomReaderTest extends Specification{

 "the movie search for today" should {

    "return data" in new WithWS{
      val belgacom = new BelgacomReader(ws)
      val today = new DateMidnight
      val result = Await.result(belgacom.readMovies(today), 60.seconds)
      //println(result.map(_.channelName).toSet)
      //	      result.map{ movie =>
      //	          println(movie)
      //	      }
      val movie = result.head
      movie.toDateTime.toDateMidnight must be equalTo today
      movie.getProgramUrl must not contain "null"
      movie.title must not beNull
      //result must not be empty
    }
  }

  "the movie search for tomorrow" should {

    "return data" in new WithWS{
      val belgacom = new BelgacomReader(ws)
      val tomorrow = new DateMidnight().plusDays(1)
      val result = Await.result(belgacom.readMovies(tomorrow), 60.seconds)
      //println(result.map(_.channelName).toSet)
      //	      result.map{ movie =>
      //	          println(movie)
      //	      }
      val movie = result.head
      movie.toDateTime.toDateMidnight must be equalTo tomorrow
      movie.getProgramUrl must not contain "null"
      movie.title must not beNull
      //result must not be empty

    }
  }
}