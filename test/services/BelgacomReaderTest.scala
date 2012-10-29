package services

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import java.util.Date
import org.joda.time.DateMidnight
import java.util.concurrent.TimeUnit
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BelgacomReaderTest extends Specification {

 
 "the movie search for today" should {
    "return data" in {
        running(FakeApplication()) {
          val today = new DateMidnight
	      val result = BelgacomReader.readMovies(today).await(30, TimeUnit.SECONDS).get
//	      result.map{ movie =>
//	          println(movie)
//	      }
          val movie = result.head
          movie.toDateTime.toDateMidnight() must be eq(today)
          movie.getProgramUrl must not contain("null")
          movie.title must not beNull
	      //result must not be empty
        }
    }
  }
 
 "the movie search for tomorow" should {
    "return data" in {
        running(FakeApplication()) {
          val tomorow = new DateMidnight().plusDays(1)
	      val result = BelgacomReader.readMovies(tomorow).await(30, TimeUnit.SECONDS).get
//	      result.map{ movie =>
//	          println(movie)
//	      }
          val movie = result.head
          movie.toDateTime.toDateMidnight() must be eq(tomorow)
          movie.getProgramUrl must not contain("null")
          movie.title must not beNull
	      //result must not be empty
        }
    }
  }
}