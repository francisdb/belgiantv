package services
import play.api.Play
import play.api.libs.ws.WS
import play.api.Logger
import models.helper.TomatoesSearch
import scala.collection.JavaConversions._
import models.helper.TomatoesSearch
import models.helper.TomatoesSearch

import scala.concurrent.ExecutionContext.Implicits.global
import org.codehaus.jackson.map.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.codehaus.jackson.`type`.TypeReference
import java.lang.reflect.{Type, ParameterizedType}

object TomatoesApiService extends JacksonMapper{
  
  private lazy val apikey = PlayUtil.config("tomatoes.apikey")
  
  def find(title:String, year:Option[Int] = None) = {
    val url = "http://api.rottentomatoes.com/api/public/v1.0/movies.json"
    val q = title + year.map(" " + _).getOrElse("")
    val response = WS.url(url).withQueryString("q" -> q, "apikey" -> apikey).get()
    response.map { response =>
      val json = response.json
      val error = (json \ "error").asOpt[String]
      if (error.isDefined) {
        Logger.warn("Tomatoes api error: " + error.get)
        Logger.warn(response.body)
        None
      } else {
        val search = deserialize[TomatoesSearch](response.body)
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
    }
  }
  
  def getById(id:String) = {
    //http://api.rottentomatoes.com/api/public/v1.0/movies/13863.json
  }

  private def yearMatchIfExists(year: Option[Int], movies: List[models.helper.TomatoesMovie]) = {
    year.map { y =>
      movies.filter(m => (m.year.getOrElse(Int.MinValue) == y)).headOption.orElse(movies.headOption)
    } getOrElse {
      movies.headOption
    }
  }

}