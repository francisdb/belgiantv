package services
import play.api.Play
import play.api.libs.ws.WS
import play.api.Logger
import com.google.gson.JsonParser
import com.google.gson.Gson
import models.helper.TomatoesSearch

import scala.collection.JavaConversions._

object TomatoesApiService {
  
  private val gson = new Gson();
  
  val apikey = Play.current.configuration.getString("tomatoes.apikey").getOrElse("")
  
  def find(title:String, year:Option[Int]) = {
    val url = "http://api.rottentomatoes.com/api/public/v1.0/movies.json"
    val q = title + year.map(" " + _).getOrElse("")
    try { 
      val response = WS.url(url).withQueryString("q" -> q, "apikey" -> apikey).get().await.get
      val json = response.json
      val error = (json \ "error").asOpt[String]
      if(error.isDefined){
        Logger.warn("Tomatoes api error: " + error.get)
        None
      }else{
	    val gsonJson = new JsonParser().parse(response.body)
	    val searchResult = gson.fromJson(gsonJson, classOf[TomatoesSearch])
	    if(searchResult.movies.isEmpty){
	      None
	    }else{
	      val movies = searchResult.movies.toList
	      // putting year in the query does not seem to return the correct movie as first item
	      if(year.isDefined){
		      Option(movies.filter(m => (m.year == year.get.toString())))
		    }else{
		      Option(movies.head)
	      }
	    }
      }
    } catch {
      case ex: Exception =>
        Logger.error(ex.getMessage(), ex)
        None
    }
  }
  
  def getById(id:String) = {
    //http://api.rottentomatoes.com/api/public/v1.0/movies/13863.json
  }
  
  

}