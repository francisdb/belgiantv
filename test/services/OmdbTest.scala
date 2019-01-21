package services

import org.specs2.mutable._
import helper.{ConfigSpec, WithWsClient}
import services.omdb.OmdbApiService

import scala.concurrent._
import scala.concurrent.duration._

class OmdbTest extends Specification with WithWsClient with ConfigSpec{

  private val configProperty = "omdb.apikey"

  "the search for Pulp Fiction" should {

    "return the correct movie" in {
      skipIfMissingConfig(configProperty)
      val omdb = new OmdbApiService(ws)

      val movieOpt = Await.result(omdb.find("Pulp Fiction"), 30.seconds)
      movieOpt must beSome
      movieOpt.get.title must startWith("Pulp Fiction")
      movieOpt.get.released must contain("1994")

    }
  }

  "the search for don 2006" should {
    "return the correct movie" in {
      skipIfMissingConfig(configProperty)
      val omdb = new OmdbApiService(ws)
      val movieOption = Await.result(omdb.find("don", Option.apply(2006)), 30.seconds)
      movieOption must beSome
      val movie = movieOption.get
      movie.title must be equalTo "Don"
      movie.released must contain("2006")

    }
  }

  "the search for hafsjkdhfkdjshadslkfhaskf" should {
    "return nothing" in {
      skipIfMissingConfig(configProperty)
      val omdb = new OmdbApiService(ws)
      val movieOption = Await.result(omdb.find("hafsjkdhfkdjshadslkfhaskf"), 30.seconds)
      movieOption must beNone

    }
  }

  "the search for The Little Rascals Save the Day Some(2013)" should {
    "return nothing" in {
      skipIfMissingConfig(configProperty)
      val omdb = new OmdbApiService(ws)
      val movieOption = Await.result(omdb.find("Little Rascals Save the Day", Some(2013)), 30.seconds)
      movieOption must beNone
    }
  }
}
