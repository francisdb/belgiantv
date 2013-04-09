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
// WARN the line above causes testing issues where, multiple akka instances are launched!
class TomatoesTest extends Specification with NoTimeConversions{

  "the search for Pulp Fiction" should {
    "return the correct movie" in {
      running(FakeApplication()) {
        val movie = Await.result(TomatoesApiService.find("Pulp Fiction"), 30 seconds).get
        movie.title must startWith("Pulp Fiction")
        movie.yearAsInt.get must beEqualTo(1994)
      }
    }
  }

  "the search for don 2006" should {
    "return the correct movie" in {
      running(FakeApplication()) {
        val movieOption = Await.result(TomatoesApiService.find("don", Option(2006)), 30 seconds)
        movieOption must beSome
        val movie = movieOption.get
        movie.title must_== ("Don")
        movie.yearAsInt.get must beEqualTo(2006)
      }
    }
  }

"the search for The Beach 2000" should {
    "return the correct movie" in {
      running(FakeApplication()) {
        val movieOption = Await.result(TomatoesApiService.find("The Beach", Option(2000)), 30 seconds)
        movieOption must beSome
        val movie = movieOption.get
        movie.title must_== ("The Beach")
        movie.yearAsInt.get must beEqualTo(2000)
      }
    }
  }

  "the search for Cape Fear 1962" should {
    "return the correct movie" in {
      running(FakeApplication()) {
        val movieOption = Await.result(TomatoesApiService.find("Cape Fear", Option(1962)), 30 seconds)
        movieOption must beSome
        val movie = movieOption.get
        movie.title must_== ("Cape Fear")
        movie.yearAsInt.get must beEqualTo(1962)
      }
    }
  }

  "the search for This Is England 2006" should {
    "return the correct movie" in {
      running(FakeApplication()) {
        val movieOption = Await.result(TomatoesApiService.find("This Is England", Option(2006)), 30 seconds)
        movieOption must beSome
        val movie = movieOption.get
        movie.title must_== ("This Is England")
        movie.yearAsInt.get must beEqualTo(2006)
      }
    }
  }

  "the search for a movie with the wrong year" should {
    "still return the correct movie" in {
      running(FakeApplication()) {
        val movieOption = Await.result(TomatoesApiService.find("Spring Breakdown", Option(2009)), 30 seconds)
        movieOption must beSome
        val movie = movieOption.get
        movie.title must_== ("Spring Breakdown")
        movie.yearAsInt.get must beEqualTo(2008)
      }
    }
  }

  "the search for hafsjkdhfkdjshadslkfhaskf" should {
    "return nothing" in {
      running(FakeApplication()) {
        val movieOption = Await.result(TomatoesApiService.find("hafsjkdhfkdjshadslkfhaskf"), 30 seconds)
        movieOption must beNone
      }
    }
  }
}