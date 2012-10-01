package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import play.api.Play.current
import play.api.libs.concurrent._
import java.io.StringReader
import java.io.ByteArrayOutputStream
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import services.HumoReader
import org.joda.time.DateMidnight
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat
import services.YeloReader
import models.Movie
import models.Broadcast
import akka.actor.Props
import services.akka.Master
import services.akka.Start
import models.helper.BroadcastInfo
import org.joda.time.Interval
import akka.actor.ActorRef
import org.joda.time.DateTimeZone
import java.sql.Date
import org.joda.time.DateTime
import org.joda.time.DateMidnight

object Application extends Controller {

  val channelFilter = List("één", "Canvas", "VTM", "2BE", "VT4", "VIJFtv", "Ketnet", "Acht", "Ned 1", "Ned 2", "Ned 3", "Vitaya", "BBC 1", "BBC 2", "BBC 3")
  val timezone = DateTimeZone.forID("Europe/Brussels")
  
  var masterActorRef: ActorRef = null

  def index = Action { implicit request =>
    
    // anything that has started more than an hour ago is not interesting
    val start = new DateTime().minusHours(1)
    // for seven days in the future (midnight)
    val end = new DateMidnight().plusDays(7)
    
    val broadcasts = Broadcast.findByInterval(new Interval(start, end))
    
    val infos = broadcasts.map{ broadcast =>
      val movie = broadcast.imdbId.flatMap(Movie.findByImdbId(_))
      BroadcastInfo(broadcast, movie)
    }
    
    val sorted = infos.sortWith(
        _.movie.map(m => m.rating).getOrElse(0.0)
        > 
        _.movie.map(m => m.rating).getOrElse(0.0))  
    
    Ok(views.html.index(sorted))
  }
  
  def scan = Action {
    masterActorRef ! Start
    Redirect(routes.Application.index).flashing("message" -> "Started database update...")
  }
  


}