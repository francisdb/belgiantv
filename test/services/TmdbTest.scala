package services

import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import org.specs2.time.NoTimeConversions

import play.api.test._
import play.api.test.Helpers._
import org.junit.runner.RunWith

import scala.concurrent._
import scala.concurrent.duration._

//@RunWith(classOf[JUnitRunner])
class TmdbTest extends Specification with NoTimeConversions {

  skipAllUnless(PlayUtil.configExists("tmdb.apikey"))

  "the search for don 2006" should {
    "return the correct movie" in {
        running(FakeApplication()) {
	        val movie = Await.result(TmdbApiService.find("don", Option.apply(2006)), 30 seconds).get
	        movie.title must_== "Don"
	        movie.release_date must startWith("2006")
        }
    }
  }
  
  "the search for I, Robot 2004" should {
    "return the correct movie" in {
        running(FakeApplication()) {
	        val movie = Await.result(TmdbApiService.find("I, Robot", Option.apply(2004)), 30 seconds).get
	        movie.title must_== "I, Robot"
	        movie.release_date must startWith("2004")
        }
    }
  }
  
  "the search for I, Robot 2004" should {
    "return the correct movie" in {
        running(FakeApplication()) {
          val movie = Await.result(TmdbApiService.find("I, Robot", Option.apply(2004)), 30 seconds).get
	        movie.title must_== "I, Robot"
	        movie.release_date must startWith("2004")
        }
    }
  }
  
  "the search for L'immortel 2010" should {
    "return the correct movie" in {
        running(FakeApplication()) {
          val movie = Await.result(TmdbApiService.find("L'immortel", Option.apply(2010)), 30 seconds).get
	        // international title
	        movie.title must startWith("22 Bullets")
	        movie.release_date must startWith("2010")
        }
    }
  }
  
  "the search for hafsjkdhfkdjshadslkfhaskf" should {
    "return nothing" in {
        running(FakeApplication()) {
          val movieOption = Await.result(TmdbApiService.find("hafsjkdhfkdjshadslkfhaskf"), 30 seconds)
          movieOption must beNone
        }
    }
  }
}
