package services

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import java.util.concurrent.TimeUnit
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TomatoesTest extends Specification {

  "the search for Pulp Fiction" should {
    "return the correct movie" in {
      running(FakeApplication()) {
        val result = TomatoesApiService.find("Pulp Fiction")
        val movie = result.await(30, TimeUnit.SECONDS).get.get
        movie.title must startWith("Pulp Fiction")
        movie.year.get must beEqualTo(1994)
      }
    }
  }

  "the search for don 2006" should {
    "return the correct movie" in {
      running(FakeApplication()) {
        val result = TomatoesApiService.find("don", Option(2006))
        val movieOption = result.await(30, TimeUnit.SECONDS).get
        movieOption must beSome
        val movie = movieOption.get
        movie.title must_== ("Don")
        movie.year.get must beEqualTo(2006)
      }
    }
  }

"the search for The Beach 2000" should {
    "return the correct movie" in {
      running(FakeApplication()) {
        val result = TomatoesApiService.find("The Beach", Option(2000))
        val movieOption = result.await(30, TimeUnit.SECONDS).get
        movieOption must beSome
        val movie = movieOption.get
        movie.title must_== ("The Beach")
        movie.year.get must beEqualTo(2000)
      }
    }
  }

  "the search for Cape Fear 1962" should {
    "return the correct movie" in {
      running(FakeApplication()) {
        val result = TomatoesApiService.find("Cape Fear", Option(1962))
        val movieOption = result.await(30, TimeUnit.SECONDS).get
        movieOption must beSome
        val movie = movieOption.get
        movie.title must_== ("Cape Fear")
        movie.year.get must beEqualTo(1962)
      }
    }
  }

  "the search for This Is England 2006" should {
    "return the correct movie" in {
      running(FakeApplication()) {
        val result = TomatoesApiService.find("This Is England", Option(2006))
        val movieOption = result.await(30, TimeUnit.SECONDS).get
        movieOption must beSome
        val movie = movieOption.get
        movie.title must_== ("This Is England")
        movie.year.get must beEqualTo(2006)
      }
    }
  }

  "the search for a movie with the wrong year" should {
    "still return the correct movie" in {
      running(FakeApplication()) {
        val result = TomatoesApiService.find("Spring Breakdown", Option(2009))
        val movieOption = result.await(30, TimeUnit.SECONDS).get
        movieOption must beSome
        val movie = movieOption.get
        movie.title must_== ("Spring Breakdown")
        movie.year.get must beEqualTo(2008)
      }
    }
  }

  "the search for hafsjkdhfkdjshadslkfhaskf" should {
    "return nothing" in {
      running(FakeApplication()) {
        val result = TomatoesApiService.find("hafsjkdhfkdjshadslkfhaskf")
        val movie = result.await(30, TimeUnit.SECONDS).get
        movie must beNone
      }
    }
  }
}