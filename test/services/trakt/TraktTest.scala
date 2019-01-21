package services.trakt

import helper.{ConfigSpec, WithWsClient}
import org.specs2.mutable._
import play.api.{Configuration, Environment}
import services.trakt.TraktApiService.{TraktIds, TraktMovie}

import scala.concurrent._
import scala.concurrent.duration._

class TraktTest extends Specification with WithWsClient with ConfigSpec{

  private val configPropertyId = "trakt.client.id"
  private val configPropertySecret = "trakt.client.secret"

  private lazy val configuration = Configuration.load(Environment.simple())
  private lazy val traktConfig = new TraktConfig(configuration)
  private lazy val trakt = new TraktApiService(traktConfig, ws)


  "the search for Pulp Fiction" should {

    "find the correct movie" in {
      skipIfMissingConfig(configPropertyId)
      val movieOpt = Await.result(trakt.find("Pulp Fiction"), 30.seconds)
      movieOpt must beSome(
        TraktMovie(
          "Pulp Fiction",
          Some(1994),
          TraktIds(
            trakt = 554,
            slug = "pulp-fiction-1994",
            imdb = Some("tt0110912"),
            tmdb = 680
          )
        )
      )
    }

    "return movie rating" in {
      skipIfMissingConfig(configPropertyId)
      val ratingOpt = Await.result(trakt.movieRating(554), 30.seconds)
      //println(ratingOpt)
      ratingOpt must beSome
    }
  }

  "the search for Taxi Driver 1976"  should  {
    "yield the correct result" in {
      skipIfMissingConfig(configPropertyId)
      val movieOpt = Await.result(trakt.find("Taxi Driver", Some(1976)), 30.seconds)
      movieOpt must beSome(
        TraktMovie(
          "Taxi Driver",
          Some(1976),
          TraktIds(
            trakt = 72,
            slug = "taxi-driver-1976",
            imdb = Some("tt0075314"),
            tmdb = 103
          )
        )
      )
    }
  }

  "the search for Taxi 2004"  should  {
    "yield the correct result" in {
      skipIfMissingConfig(configPropertyId)
      val movieOpt = Await.result(trakt.find("Taxi", Some(2004)), 30.seconds)
      movieOpt must beSome(
        TraktMovie(
          "Taxi",
          Some(2004),
          TraktIds(
            trakt = 6182,
            slug = "taxi-2004",
            imdb = Some("tt0316732"),
            tmdb = 11045
          )
        )
      )
    }
  }

}
