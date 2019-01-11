package services
import play.api.libs.ws.WSClient
import play.api.Logger
import models.helper.{TomatoesMovie, TomatoesSearch}

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.util.control.NonFatal

class TomatoesApiService(tomatoesConfig: TomatoesConfig, ws: WSClient){

  def find(title:String, year:Option[Int] = None): Future[Option[TomatoesMovie]] = {
    val url = "http://api.rottentomatoes.com/api/public/v1.0/movies.json"
    val q = title + year.map(" " + _).getOrElse("")
    val response = ws.url(url).addQueryStringParameters("q" -> q, "apikey" -> tomatoesConfig.apiKey).get()
    response.map { response =>
      val json = try{
        response.json
      }catch{
        // fake error response for non-json response
        case NonFatal(e) => Json.obj(
          "error" -> response.body.take(100)
        )
      }
      val error = (json \ "error").asOpt[String]
      if (error.isDefined) {
        Logger.warn("Tomatoes api error: " + error.get)
        Logger.warn(response.body)
        None
      } else {

        import models.helper.TomatoesReaders._
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

  private def yearMatchIfExists(year: Option[Int], movies: List[models.helper.TomatoesMovie]) = {
    year.map { y =>
      movies.find(m => m.year.getOrElse(Int.MinValue) == y).orElse(movies.headOption)
    } getOrElse {
      movies.headOption
    }
  }

}
