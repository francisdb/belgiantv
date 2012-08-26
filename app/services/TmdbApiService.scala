package services

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.io.IOException

import play.api.libs.ws.WS
import play.api.libs.concurrent.Promise
import play.api.Logger
import play.api.Play

import org.codehaus.jackson.map.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.codehaus.jackson.`type`.TypeReference
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import models.helper.TmdbMovieSearch
import models.helper.TmdbMovieSearchPager
import models.helper.TmdbMovie


object TmdbApiService {
	
	val BASE = "http://api.themoviedb.org/3"
	  
	private val logger = Logger("application.tmdb")

	private val apiKey = Play.current.configuration.getString("tmdb.apikey")
	  .getOrElse(throw new RuntimeException("Missing tmdb api key"))
	
	private val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

	def findOrRead(title:String, year:Option[Int] = None): Promise[Option[TmdbMovieSearch]] = {
		if(title==null){
			throw new IllegalArgumentException("NULL title")
		}
		val result = search(title, year)
		// TODO fall back to title only search if none found?
		// create test to see if this is needed
		result.map(list => list.headOption)
	}
	
	
	def search(title:String, year:Option[Int] = None) = {
	  
		val url = BASE + "/search/movie";
		
		val qs = List(("api_key"-> apiKey), ("query" -> title))
		val qs2 = year.map(y => qs.+:("year" -> y.toString)).getOrElse(qs)
		
		logger.info(url)
		val response = WS.url(url)
		  .withHeaders("Accept" -> "application/json")
		  .withQueryString(qs:_*).get
		  
		response.map{ r =>
		  logger.info(url + " " + r.status)
		  r.status match {
		    case 503 => 
		      logger.error(r.status + " " + r.ahcResponse.getStatusText() + " " + r.body)
		      List()
		    case _ => 
		      val pager = deserialize[TmdbMovieSearchPager](r.body)
		      pager.results
		  }
	      
		}
	}
	
	private def deserialize[T: Manifest](value: String) : T =
    mapper.readValue(value, new TypeReference[T]() {
      override def getType = new ParameterizedType {
        val getActualTypeArguments = manifest[T].typeArguments.map(_.erasure.asInstanceOf[Type]).toArray
        val getRawType = manifest[T].erasure
        val getOwnerType = null
      }
  })
}
