package services

import org.specs2.mutable._

import helper.WithWsClient

import scala.concurrent._
import scala.concurrent.duration._

class ImdbTest extends Specification with WithWsClient {

  "the search for Pulp Fiction" should {

    "return the correct movie" in {
      skipped("not working any longer, do we need to become a patron?")
      val imdb = new ImdbApiService(ws)

      val movieOpt = Await.result(imdb.find("Pulp Fiction"), 30.seconds)
      movieOpt must beSome
      movieOpt.get.title must startWith("Pulp Fiction")
      movieOpt.get.released must contain("1994")

    }
  }

  "the search for don 2006" should {
    "return the correct movie" in {
      skipped("Currently returns 'The service is unavailable.', strangely other requests succeed")
      val imdb = new ImdbApiService(ws)
      val movieOption = Await.result(imdb.find("don", Option.apply(2006)), 30.seconds)
      movieOption must beSome
      val movie = movieOption.get
      movie.title must be equalTo "Don"
      movie.released must contain("2006")

    }
  }

  "the search for hafsjkdhfkdjshadslkfhaskf" should {
    "return nothing" in {
      val imdb = new ImdbApiService(ws)
      val movieOption = Await.result(imdb.find("hafsjkdhfkdjshadslkfhaskf"), 30.seconds)
      movieOption must beNone

    }
  }
}