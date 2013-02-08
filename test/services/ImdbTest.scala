package services

import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import org.specs2.time.NoTimeConversions

import play.api.test._
import play.api.test.Helpers._
import java.util.concurrent.TimeUnit
import org.junit.runner.RunWith

import scala.concurrent._
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class ImdbTest extends Specification with NoTimeConversions{
  "the search for Pulp Fiction" should {
    "return the correct movie" in {
        running(FakeApplication()) {
	        val movie = Await.result(ImdbApiService.find("Pulp Fiction"), 30 seconds).get
	        movie.title must startWith("Pulp Fiction")
	        movie.released must contain("1994")
        }
    }
  }
  
  "the search for don 2006" should {
    "return the correct movie" in {
        running(FakeApplication()) {
	        val movieOption = Await.result(ImdbApiService.find("don", Option.apply(2006)), 30 seconds)
	        movieOption must beSome
	        val movie = movieOption.get
	        movie.title must_== ("Don")
	        movie.released must contain("2006")
        }
    }
  }
  
  "the search for hafsjkdhfkdjshadslkfhaskf" should {
    "return nothing" in {
        running(FakeApplication()) {
	        val movieOption = Await.result(ImdbApiService.find("hafsjkdhfkdjshadslkfhaskf"), 30 seconds)
          movieOption must beNone
        }
    }
  }
}