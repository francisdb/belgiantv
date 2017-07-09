package services

import org.specs2.mutable._

import scala.concurrent._
import scala.concurrent.duration._
import helper.{ConfigSpec, WithWsClient}

class TmdbTest extends Specification with ConfigSpec with WithWsClient {

  private val configProperty = "tmdb.apikey"

  // TODO see if we can read the play config without starting the application
  // skipAllUnless(PlayUtil.configExists("tmdb.apikey"))

  "the search for don 2006" should {

    "return the correct movie" in {
      skipIfMissingConfig(configProperty)
      val tmdb = new TmdbApiService(ws)
      val movie = Await.result(tmdb.find("don", Option.apply(2006)), 30.seconds).get
      movie.title must_== "Don"
      movie.release_date must startWith("2006")
      //print(movie.vote_average)
      (movie.vote_average must not).beNull

    }
  }

  "the search for I, Robot 2004" should {

    "return the correct movie" in {
      skipIfMissingConfig(configProperty)
      val tmdb = new TmdbApiService(ws)
      val movie = Await.result(tmdb.find("I, Robot", Option.apply(2004)), 30.seconds).get
      movie.title must_== "I, Robot"
      movie.release_date must startWith("2004")

    }
  }

  "the search for I, Robot 2004" should {

    "return the correct movie" in {
      skipIfMissingConfig(configProperty)
      val tmdb = new TmdbApiService(ws)
      val movie = Await.result(tmdb.find("I, Robot", Option.apply(2004)), 30.seconds).get
      movie.title must_== "I, Robot"
      movie.release_date must startWith("2004")

    }
  }

  "the search for L'immortel 2010" should {

    "return the correct movie" in {
      skipIfMissingConfig(configProperty)
      val tmdb = new TmdbApiService(ws)
      val movie = Await.result(tmdb.find("L'immortel", Option.apply(2010)), 30.seconds).get
      println(movie)
      // international title
      movie.title must startWith("22 Bullets")
      movie.release_date must startWith("2010")

    }
  }

  "the search for hafsjkdhfkdjshadslkfhaskf" should {

    "return nothing" in {
      skipIfMissingConfig(configProperty)
      val tmdb = new TmdbApiService(ws)
      val movieOption = Await.result(tmdb.find("hafsjkdhfkdjshadslkfhaskf"), 30.seconds)
      movieOption must beNone

    }
  }

  "the search for Relative Chaos (2006)" should {

    "return no score as it is 0 %" in {
      skipIfMissingConfig(configProperty)
      val tmdb = new TmdbApiService(ws)
      val movieOption = Await.result(tmdb.find("Murdered for Being Different"), 30.seconds)
      movieOption.map(_.fixedAverage) must beSome(None)
    }
  }
}
