package services

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.io.IOException

import play.api.libs.ws.WS
import play.api.libs.concurrent.Promise
import play.api.Logger
import play.api.Play

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.core.`type`.TypeReference
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import models.helper.TmdbMovieSearch
import models.helper.TmdbMovieSearchPager
import models.helper.TmdbMovie

import scala.concurrent.ExecutionContext.Implicits.global
import concurrent.Future

object TmdbApiService extends JacksonMapper{
	
	val BASE = "http://api.themoviedb.org/3"
	  
	private val logger = Logger("application.tmdb")

	private lazy val apiKey = PlayUtil.config("tmdb.apikey")

	def find(title:String, year:Option[Int] = None): Future[Option[TmdbMovieSearch]] = {
		if(title==null){
			throw new IllegalArgumentException("NULL title")
		}
		val result = search(title, year)
		// TODO fall back to title only search if none found?
		// create test to see if this is needed
		result.map(list => list.headOption)
	}
	
	
	def search(title:String, year:Option[Int] = None) = {
	  println(apiKey)
		val url = BASE + "/search/movie"
		
		val qs = List(("api_key"-> apiKey), ("query" -> title))
		val qs2 = year.map(y => qs.+:("year" -> y.toString)).getOrElse(qs)
		
		logger.info(url)
		val response = WS.url(url)
		  .withHeaders("Accept" -> "application/json")
		  .withQueryString(qs2:_*).get
		  
		response.map{ r =>
		  logger.info(url + " " + r.status)
		  r.status match {
		    case 503 => 
		      logger.error(r.status + " " + r.ahcResponse.getStatusText() + " " + r.body)
		      List()
        case 401 =>
          logger.error(r.status + " " + r.ahcResponse.getStatusText() + " " + r.body)
          List()
		    case _ => 
		      val pager = deserialize[TmdbMovieSearchPager](r.body)
		      pager.results
		  }
	      
		}
	}
}
