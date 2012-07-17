package services

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

import models.helper.TmdbMovie
import play.Logger
import play.Logger.ALogger
import play.Play
import play.api.libs.ws.WS
import play.api.libs.concurrent.Promise

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser

object TmdbApiService {
	
	val LANG_EN = "en"
	val TYPE_JSON = "json"
	//private static final String TYPE_YAML = "yaml";
	//private static final String TYPE_XML = "xml";
	
	val BASE = "http://api.themoviedb.org/2.1"
	val logger = Logger.of("application.tmdb")

	val apiKey = Play.application.configuration.getString("tmdb.apikey")

	def findOrRead(title:String, year:Option[Int] = None): Promise[Option[TmdbMovie]] = {
		if(title==null){
			throw new IllegalArgumentException("NULL title")
		}

		val result = search(title, year)
		//result.		
				
				
		result.map{ result => 
			if(result.size == 0 && year != null){
				// try a second search without year
			  // FIXME implement
//				val result2 = search(title)
//				result2.map{ result2 =>
//					Option.apply(result2(0))
//				}.orElse(None)
			  None
			}else if(result.size != 0){
				Option.apply(result(0))
			}else{
				None
			}
		}		
			
		
	}
	
	
	def search(title:String, year:Option[Int] = None) = {
	  val search = year.map(title + " " + _).getOrElse(title)
	  
		// remove question mark as this doesn't work
		val encodedSearch = urlEncodeUtf8(search).replace("%3F", "")
		
		val url = BASE + "/Movie.search/" + LANG_EN + "/" + TYPE_JSON + "/" + apiKey + "/" + encodedSearch;
		logger.info(url);
		
		
		val response = WS.url(url).withHeaders("Accept" -> "application/json").get()
		response.map{ r =>
		  println(url)
			// FIXME return a promise instead of doing get twice
			val element = new JsonParser().parse(r.body)
			
			if(element.isJsonArray() && element.getAsJsonArray().get(0).isJsonPrimitive()){
				val message = element.getAsJsonArray().get(0).getAsString()
				logger.warn(String.format("%s returns %s", url, message))
				List[TmdbMovie]()
			}else{
				val gson = new GsonBuilder().setPrettyPrinting().create()
				//System.out.println(gson.toJson(element));
				gson.fromJson(element, classOf[Array[TmdbMovie]]).toList
			}
		}
	}
	
	def getToken() = {
		val url = BASE+"/Auth.getToken/"+TYPE_JSON+"/" + apiKey
	    val request = WS.url(url).withHeaders("Accept" -> "application/json").get()
		val element = new JsonParser().parse(request.value.get.body)
		element.getAsJsonObject().get("token").getAsString()
	}
	
	def auth(token:String) = {
		// redirect here
		"http://www.themoviedb.org/auth/" + token
	}

	def urlEncodeUtf8(string: String) = {
		URLEncoder.encode(string, "UTF-8")
	}
}
