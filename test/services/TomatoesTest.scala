package services

import org.specs2.mutable._

import scala.concurrent._
import scala.concurrent.duration._
import helper.{ConfigSpec, WithWS}

class TomatoesTest extends Specification with ConfigSpec{

  private val configProperty = "tomatoes.apikey"

  // TODO see if we can read the play config without starting the application
  // skipAllUnless(PlayUtil.configExists("tomatoes.apikey"))

  "the search for Pulp Fiction" should {
    "return the correct movie" in new WithWS{
      skipIfMissingConfig(configProperty)
      val tomatoes = new TomatoesApiService(ws)
      val movie = Await.result(tomatoes.find("Pulp Fiction"), 30.seconds).get
      movie.title must startWith("Pulp Fiction")
      movie.year.get must beEqualTo(1994)
    }
  }

  "the search for don 2006" should {

    "return the correct movie" in new WithWS{
      skipIfMissingConfig(configProperty)
      val tomatoes = new TomatoesApiService(ws)
      val movieOption = Await.result(tomatoes.find("don", Option(2006)), 30.seconds)
      movieOption must beSome
      val movie = movieOption.get
      movie.title must be equalTo "Don"
      movie.year.get must be equalTo 2006
    }
  }

  "the search for The Beach 2000" should {

    "return the correct movie" in new WithWS{
      skipIfMissingConfig(configProperty)
      val tomatoes = new TomatoesApiService(ws)
      val movieOption = Await.result(tomatoes.find("The Beach", Option(2000)), 30.seconds)
      movieOption must beSome
      val movie = movieOption.get
      movie.title must be equalTo "The Beach"
      movie.year.get must be equalTo 2000
    }
  }

  "the search for Cape Fear 1962" should {

    "return the correct movie" in new WithWS{
      skipIfMissingConfig(configProperty)
      val tomatoes = new TomatoesApiService(ws)
      val movieOption = Await.result(tomatoes.find("Cape Fear", Option(1962)), 30.seconds)
      movieOption must beSome
      val movie = movieOption.get
      movie.title must be equalTo "Cape Fear"
      movie.year.get must be equalTo 1962
    }
  }

  "the search for This Is England 2006" should {

    "return the correct movie" in new WithWS{
      skipIfMissingConfig(configProperty)
      val tomatoes = new TomatoesApiService(ws)
      val movieOption = Await.result(tomatoes.find("This Is England", Option(2006)), 30.seconds)
      movieOption must beSome
      val movie = movieOption.get
      movie.title must be equalTo "This Is England"
      movie.year.get must be equalTo 2007
    }
  }

  "the search for a movie with the wrong year" should {

    "still return the correct movie" in new WithWS{
      skipIfMissingConfig(configProperty)
      val tomatoes = new TomatoesApiService(ws)
      val movieOption = Await.result(tomatoes.find("Spring Breakdown", Option(2000)), 30.seconds)
      movieOption must beSome
      val movie = movieOption.get
      movie.title must be equalTo "Spring Breakdown"
      movie.year.get must be equalTo 2009
    }
  }

  "the search for hafsjkdhfkdjshadslkfhaskf" should {

    "return nothing" in new WithWS{
      skipIfMissingConfig(configProperty)
      val tomatoes = new TomatoesApiService(ws)
      val movieOption = Await.result(tomatoes.find("hafsjkdhfkdjshadslkfhaskf"), 30 seconds)
      movieOption must beNone
    }
  }
}