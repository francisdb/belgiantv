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
	
	val LANG_EN = "en"
	val TYPE_JSON = "json"
	//private static final String TYPE_YAML = "yaml";
	//private static final String TYPE_XML = "xml";
	
	val BASE = "http://api.themoviedb.org/3"
	val logger = Logger("application.tmdb")

	val apiKey = Play.current.configuration.getString("tmdb.apikey")
	  .getOrElse(throw new RuntimeException("Missing tmdb api key"))
	
	val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

	def findOrRead(title:String, year:Option[Int] = None): Promise[Option[TmdbMovieSearch]] = {
		if(title==null){
			throw new IllegalArgumentException("NULL title")
		}
		val result = search(title, year)
		result.map(list => list.headOption)
	}
	
	
	def search(title:String, year:Option[Int] = None) = {
	  
		// remove question mark as this doesn't work
		// val encodedSearch = urlEncodeUtf8(search).replace("%3F", "")
		
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
	
	def getToken() = {
		val url = BASE+"/Auth.getToken/"+TYPE_JSON+"/" + apiKey
	    val request = WS.url(url)
	      .withHeaders("Accept" -> "application/json")
	      .withQueryString("api_key"-> apiKey).get()
		val element = request.value.get.json
		(element \ "token").asOpt[String].getOrElse(throw new RuntimeException("Missing token"))
	}
	
	def auth(token:String) = {
		// redirect here
		"http://www.themoviedb.org/auth/" + token
	}

	def urlEncodeUtf8(string: String) = {
		URLEncoder.encode(string, "UTF-8")
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
