package services.tomatoes

import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.Future
import scala.util.control.NonFatal

import TomatoesReaders._

class TomatoesApiService(tomatoesConfig: TomatoesConfig, ws: WSClient) {

  val logger = Logger("application.tomatoes")

  import scala.concurrent.ExecutionContext.Implicits.global

  def find(title: String, year: Option[Int] = None): Future[Option[TomatoesMovie]] = {
    val url      = "http://api.rottentomatoes.com/api/public/v1.0/movies.json"
    val q        = title + year.map(" " + _).getOrElse("")
    val response = ws.url(url).addQueryStringParameters("q" -> q, "apikey" -> tomatoesConfig.apiKey).get()
    response.map { response =>
      val json = try {
        response.json
      } catch {
        // fake error response for non-json response
        case NonFatal(e) =>
          Json.obj(
            "error" -> response.body.take(100)
          )
      }
      val error = (json \ "error").asOpt[String]
      if (error.isDefined) {
        logger.warn("Tomatoes api error: " + error.get)
        logger.warn(response.body)
        None
      } else {
        val search = Json.fromJson[TomatoesSearch](json)
        search.fold(
          invalid => {
            None
          },
          search => {
            //val search = deserialize[TomatoesSearch](response.body)
            val movies = search.movies
            // putting year in the query does not seem to return the correct movie as first item
            if (movies.isEmpty) {
              None
            } else {
              // we probably want to be a bit smarter and use some Levenshtein distance lib
              // http://en.wikipedia.org/wiki/Approximate_string_matching
              val exact = movies.filter(_.title.equalsIgnoreCase(title))
              yearMatchIfExists(year, exact).orElse(yearMatchIfExists(year, movies))
            }
          }
        )
      }
    }
  }

//  def getById(id:String) = {
//    //http://api.rottentomatoes.com/api/public/v1.0/movies/13863.json
//  }

  private def yearMatchIfExists(year: Option[Int], movies: List[TomatoesMovie]) =
    year.map { y =>
      movies.find(m => m.year.getOrElse(Int.MinValue) == y).orElse(movies.headOption)
    } getOrElse {
      movies.headOption
    }

}
