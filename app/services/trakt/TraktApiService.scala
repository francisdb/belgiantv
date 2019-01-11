package services.trakt

import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WSClient
import services.trakt.TraktApiService.{TraktMovie, TraktMovieSearchResult}

import scala.concurrent.Future

/**
  * Docs available at https://trakt.docs.apiary.io
  */
object TraktApiService{

  case class TraktIds(
    trakt: Int,
    slug: String,
    imdb: Option[String],
    tmdb: Int
  )
  case class TraktMovie(
    title: String,
    year: Option[Int],
    ids: TraktIds
  )
  case class TraktMovieSearchResult(
    movie: TraktMovie,
    score: Double,
    // distribution not used
  )

  implicit val traktIdsReads = Json.reads[TraktIds]
  implicit val traktMovieReads = Json.reads[TraktMovie]
  implicit val traktMovieSearchResultReads = Json.reads[TraktMovieSearchResult]

  // movies
//  [{
//     "type":"movie",
//     "score":1000,
//     "movie":{
//       "title":"Pulp Fiction",
//       "year":1994,
//       "ids":{
//         "trakt":554,
//         "slug":"pulp-fiction-1994",
//         "imdb":"tt0110912",
//         "tmdb":680
//        }
//      }
//    }, ...


}

class TraktApiService(traktConfig: TraktConfig, ws: WSClient) {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val logger = Logger("application.omdb")

  private val base = "https://api.trakt.tv"

  def find(title: String, year: Option[Int] = None): Future[Option[TraktMovie]] = {
    val request = ws
      .url(base + s"/search/movie")
      .addQueryStringParameters("query" -> title)
      .addHttpHeaders("trakt-api-key" -> traktConfig.clientId)

    logger.info(s"Fetching $request")

    request.get().map { response =>

      response.status match {
        case Status.OK =>
          response.json.validate[Seq[TraktMovieSearchResult]] match {
            case JsSuccess(list, _) =>
              val movies = list
                .filter(_.score > 1)
                .map(_.movie)
              val withYearFilter = year.fold(movies){ year =>
                movies.filter(_.year.contains(year))
              }
              withYearFilter.headOption
            case JsError(e) =>
              throw new IllegalStateException(e.toString())
          }

        case other =>
          logger.error(s"Got $other for ${request.uri} - ${response.body}")
          None
      }
    }
  }

  def movieRating(traktId: Int): Future[Option[Float]] = {

    val request = ws
      .url(base + s"/movies/$traktId/ratings")
      .addHttpHeaders("trakt-api-key" -> traktConfig.clientId)

    logger.info(s"Fetching $request")

    request.get().map { response =>
      response.status match {
        case Status.OK =>
          Some((response.json \ "rating").as[Float])
        case other =>
          logger.error(s"Got $other for ${request.uri} - ${response.body}")
          None
      }
    }
  }

}
