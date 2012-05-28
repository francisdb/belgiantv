package services
import play.api.libs.ws.WS
import com.google.gson.Gson
import com.google.gson.JsonParser
import play.api.Logger
import models.ImdbApiMovie

object ImdbApiService {

  private val gson = new Gson();

  def findOrRead(title: String, year: Option[Int] = None): Option[ImdbApiMovie] = {
    val url = "http://www.imdbapi.com/"
    var request = WS.url(url).withQueryString("t" -> title)
    if (year.isDefined) {
      request = request.withQueryString("y" -> year.get.toString())
    }
    
    try {
      val response = request.get().await.get      
      val json = response.json
      val error = (json \ "Error").asOpt[String]
      if(error.isDefined){
        Logger.warn("IMDB api internal error: " + error.get)
        None
      }else{
	    val gsonJson = new JsonParser().parse(response.body)
	    Option(gson.fromJson(gsonJson, classOf[ImdbApiMovie]))
      }
    } catch {
      case ex: Exception =>
        Logger.error(ex.getMessage(), ex)
        None
    }

  }
}