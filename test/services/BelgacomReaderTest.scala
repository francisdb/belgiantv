package services


import play.api.test.Helpers._

import org.joda.time.DateMidnight


import org.specs2.mutable._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import play.api.test.FakeApplication

class BelgacomReaderTest extends Specification{

 "the movie search for today" should {
    "return data" in {
        running(FakeApplication()) {
          val today = new DateMidnight
	        val result = Await.result(BelgacomReader.readMovies(today), 60.seconds)
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
  }
 
 "the movie search for tomorrow" should {
    "return data" in {
        running(FakeApplication()) {
          val tomorrow = new DateMidnight().plusDays(1)
	        val result = Await.result(BelgacomReader.readMovies(tomorrow), 60.seconds)
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
}