package services

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import java.util.concurrent.TimeUnit

class ImdbTest extends Specification {
  "the search for Pulp Fiction" should {
    "return the correct movie" in {
        running(FakeApplication()) {
	        val result = ImdbApiService.findOrRead("Pulp Fiction")
	        val movie = result.await(30, TimeUnit.SECONDS).get.get
	        movie.title must startWith("Pulp Fiction")
	        movie.released must contain("1994")
        }
    }
  }
  
  "the search for don 2006" should {
    "return the correct movie" in {
        running(FakeApplication()) {
	        val result = ImdbApiService.findOrRead("don", Option.apply(2006))
	        val movieOption = result.await(30, TimeUnit.SECONDS).get
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
	        val result = ImdbApiService.findOrRead("hafsjkdhfkdjshadslkfhaskf")
	        val movie = result.await(30, TimeUnit.SECONDS).get
	        movie must beNone
        }
    }
  }
}