package services
import play.api.libs.ws.WS
import play.api.Logger
import models.helper.ImdbApiMovie
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.core.`type`.TypeReference
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

import scala.concurrent.ExecutionContext.Implicits.global

object ImdbApiService extends JacksonMapper {

  private val logger = Logger("application.imdb")


  def find(title: String, year: Option[Int] = None) = {

    // TODO fallback to a search without year if no result was found for a search including the year

    //val url = "http://www.imdbapi.com/"
    val url = "http://www.omdbapi.com/"
    var request = WS.url(url).withQueryString("t" -> title)
    if (year.isDefined) {
      request = request.withQueryString("y" -> year.get.toString())
    }
    
    logger.info("Fetching " + request)
    
    request.get.map{ response =>
	      val json = response.json
	      val error = (json \ "Error").asOpt[String]
	      if(error.isDefined){
	        val msg = "IMDB api internal error for %s %s: %s".format(title, year, error.get)
	        logger.warn(msg)
	        None
	      }else{
	        val movie = deserialize[ImdbApiMovie](response.body)
	        Option(movie)
	      }
    }
  }

}