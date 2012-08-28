package services

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import java.util.Date

class BelgacomReaderTest extends Specification {
 "the search for today" should {
    "return data" in {
      //pending("ignored")
        running(FakeApplication()) {
	        val result = BelgacomReader.read(new Date)
	        result.map{ r =>
	          println(r.channelname)
	          r.pr.map{ program =>
	            //println(" - " + program.getStart + " " + program.title)
	          }
	        }
	        result must not be empty
        }
    }
  }
 
 "the movie search for today" should {
    "return data" in {
        running(FakeApplication()) {
	        val result = BelgacomReader.readMovies(new Date, 0)
	        result.map{ movie =>
	          println(movie.channelname + " " + movie.getStart + " " + movie.title)
	        }
	        result must not be empty
        }
    }
  }
}