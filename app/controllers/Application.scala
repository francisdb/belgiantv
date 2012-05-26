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
import services.YeloReader2
import play.api.libs.concurrent.Promise

object Application extends Controller {

  val channelFilter = List("Canvas", "VTM", "2BE", "VT4", "VIJFtv", "Ketnet", "Ned 3", "Vitaya")

  def index = Action { implicit request =>
    Ok(views.html.index())
  }
  
  def humo = Action {
    val date = new DateMidnight()
    val url = HumoReader.urlForDay(date)
    println(url);
    
    Async {
      WS.url(url).get().map { response =>

        val movies = HumoReader.parseDay(date, response.body).filter(result => channelFilter.contains(result.channel))
        movies.foreach(movie => println(movie.readable))
        Redirect(routes.Application.index).flashing("message" -> (movies.length + " movies found on humo"))
      }
    }
  }
  
  def yelo = Action {
    val date = new DateMidnight()
    val url = YeloReader2.urlForDay(date)
    println(url);

    Async {
      WS.url(url).get().map { response =>

        val movies = YeloReader2.parseDay(date, response.json)
        //movies.foreach(movie => println(movie))
        Redirect(routes.Application.index).flashing("message" -> (movies.length + " events found on yelo"))
      }
    }
  }

}