package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import java.io.StringReader
import java.io.ByteArrayOutputStream
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import services.HumoReader
import org.joda.time.DateMidnight
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat



object Application extends Controller {
  
  val formatter = DateTimeFormat.forPattern("YYYY-MM-dd")
  
  val channelFilter = List("Canvas", "VTM", "2BE", "VT4", "VIJFtv", "Ketnet", "Ned 3", "Vitaya")	 
  
  def index = Action {
    
    
    // http://www.humo.be/tv-gids/2012-05-26
    // http://www.humo.be/tv-gids/2012-05-26/genres/film
    
    val date = new DateMidnight()
    
    
    val url = "http://www.humo.be/tv-gids/"+formatter.print(date)+"/genres/film"
    println(url);
    
    Async {
	    WS.url(url).get().map { response =>
	      
	      val movies = HumoReader.parseDay(date, response.body).filter( result => channelFilter.contains(result.channel))
	      movies.foreach(movie => println(movie.readable))
	      Ok("ok")
	    }
	  } 
  }
  
}