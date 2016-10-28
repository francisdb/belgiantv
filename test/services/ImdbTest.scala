package services

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import java.util.concurrent.TimeUnit

import helper.{TestApplicationBuilder, WithWS}

import scala.concurrent._
import scala.concurrent.duration._

class ImdbTest extends Specification{

  "the search for Pulp Fiction" should {

    "return the correct movie" in new WithWS{
      val imdb = new ImdbApiService(ws)

      val movie = Await.result(imdb.find("Pulp Fiction"), 30.seconds).get
      movie.title must startWith("Pulp Fiction")
      movie.released must contain("1994")

    }
  }

  "the search for don 2006" should {
    "return the correct movie" in new WithWS{
      val imdb = new ImdbApiService(ws)
      val movieOption = Await.result(imdb.find("don", Option.apply(2006)), 30.seconds)
      movieOption must beSome
      val movie = movieOption.get
      movie.title must be equalTo "Don"
      movie.released must contain("2006")

    }
  }

  "the search for hafsjkdhfkdjshadslkfhaskf" should {
    "return nothing" in new WithWS{
      val imdb = new ImdbApiService(ws)
      val movieOption = Await.result(imdb.find("hafsjkdhfkdjshadslkfhaskf"), 30.seconds)
      movieOption must beNone

    }
  }
}