package services

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import java.util.concurrent.TimeUnit

class TmdbTest extends Specification {

  "the search for don 2006" should {
    "return the correct movie" in {
        running(FakeApplication()) {
	        val result = TmdbApiService.findOrRead("don", Option.apply(2006))
	        val movie = result.await(30, TimeUnit.SECONDS).get.get
	        
	        movie.name must_== ("Don")
	        movie.released must startWith("2006")
        }
    }
  }
  
  "the search for Pulp Fiction" should {
    "return the correct movie" in {
        running(FakeApplication()) {
	        val result = TmdbApiService.findOrRead("Pulp Fiction")
	        val movie = result.await(30, TimeUnit.SECONDS).get.get
	        movie.name must startWith("Pulp Fiction")
	        movie.released must startWith("1994")
        }
    }
  }
}
