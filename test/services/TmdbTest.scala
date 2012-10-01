package services

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import java.util.concurrent.TimeUnit
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
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
  
  "the search for I, Robot 2004" should {
    "return the correct movie" in {
        running(FakeApplication()) {
	        val result = TmdbApiService.find("I, Robot", Option.apply(2004))
	        val movie = result.await(30, TimeUnit.SECONDS).get.get
	        movie.title must_== ("I, Robot")
	        movie.release_date must startWith("2004")
        }
    }
  }
  
  "the search for I, Robot 2004" should {
    "return the correct movie" in {
        running(FakeApplication()) {
	        val result = TmdbApiService.find("I, Robot", Option.apply(2004))
	        val movie = result.await(30, TimeUnit.SECONDS).get.get
	        movie.title must_== ("I, Robot")
	        movie.release_date must startWith("2004")
        }
    }
  }
  
  "the search for L'immortel 2010" should {
    "return the correct movie" in {
        running(FakeApplication()) {
	        val result = TmdbApiService.find("L'immortel", Option.apply(2010))
	        val movie = result.await(30, TimeUnit.SECONDS).get.get
	        // international title
	        movie.title must startWith("22 Bullets")
	        movie.release_date must startWith("2010")
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
