package services

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import java.util.concurrent.TimeUnit

class TmdbTest extends Specification {

  "the search for don 2006" should {
    "return the correct movie" in {
        running(FakeApplication()) {
	        val result = TmdbApiService.find("don", Option.apply(2006))
	        val movie = result.await(30, TimeUnit.SECONDS).get.get
	        movie.title must_== ("Don")
	        movie.release_date must startWith("2006")
        }
    }
  }
  
  "the search for Pulp Fiction" should {
    "return the correct movie" in {
        running(FakeApplication()) {
	        val result = TmdbApiService.find("Pulp Fiction")
	        val movie = result.await(30, TimeUnit.SECONDS).get.get
	        movie.title must startWith("Pulp Fiction")
	        movie.release_date must startWith("1994")
        }
    }
  }
  
  "the search for hafsjkdhfkdjshadslkfhaskf" should {
    "return nothing" in {
        running(FakeApplication()) {
	        val result = TmdbApiService.find("hafsjkdhfkdjshadslkfhaskf")
	        val movie = result.await(30, TimeUnit.SECONDS).get
	        movie must beNone
        }
    }
  }
}
