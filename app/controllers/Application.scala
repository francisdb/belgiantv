package controllers

import _root_.models.helper.BroadcastInfo
import _root_.models.{Channel, Movie, Broadcast}

import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.libs.concurrent._

import java.io.StringReader
import java.io.ByteArrayOutputStream
import services.YeloReader


import services.akka.{StartTomatoes, Start}
//import akka.actor.Props
import akka.actor.ActorRef

import org.joda.time.Interval
import org.joda.time.DateTimeZone
import org.joda.time.DateTime
import org.joda.time.DateMidnight



import play.modules.reactivemongo.{MongoController, ReactiveMongoPlugin}



import scala.concurrent.{ExecutionContext, Future}

// Play Json imports
import play.api.libs.json._

object Application extends Controller with MongoController {

  // TODO put this somewhere else or copy to each place where used?
  val db = ReactiveMongoPlugin.db

  val timezone = DateTimeZone.forID("Europe/Brussels")
  
  var masterActorRef: ActorRef = null

  def index = Action { implicit request =>
    
    // anything that has started more than an hour ago is not interesting
    val start = new DateTime().minusHours(1)
    // for seven days in the future (midnight)
    val end = new DateMidnight().plusDays(7)
    val interval = new Interval(start, end)

    Async {
      for{
        broadcasts <- Broadcast.findByInterval(interval)
        infos <- Future.traverse(broadcasts)(broadcast => linkWithMovie(broadcast))
      }yield{
        val sorted = infos.sortWith(BroadcastInfo.scoreSorter)
        Ok(views.html.index(sorted))
      }
    }
  }


  def linkWithMovie(broadcast: Broadcast): Future[BroadcastInfo] = {
    implicit val reader = Movie.MovieBSONReader

    // TODO find a better place to do this?
    val broadcastHd = broadcast.copy(yeloUrl = broadcast.yeloUrl.map(YeloReader.toHDUrl(_)))

    broadcastHd.imdbId match {
      case Some(imdbId) => Movie.findByImdbId(imdbId).map(BroadcastInfo(broadcastHd, _))
      case None => Future.successful(BroadcastInfo(broadcastHd, None))
    }
  }

  def scan = Action {
    masterActorRef ! Start
    Redirect(routes.Application.index).flashing("message" -> "Started database update...")
  }

  def tomatoes = Action {
    masterActorRef ! StartTomatoes
    Redirect(routes.Application.index).flashing("message" -> "Started tomatoes update...")
  }
  


}