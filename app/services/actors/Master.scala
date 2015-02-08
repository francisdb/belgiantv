package services.actors

import _root_.akka.actor.{Actor, Props}
import _root_.akka.routing.RoundRobinRouter
import play.api.Logger
import org.joda.time.DateMidnight
import akka.actor.ActorLogging
import models.Broadcast

import scala.concurrent.ExecutionContext.Implicits.global
import akka.event.LoggingReceive

class Master extends MailingActor with LoggingActor with ActorLogging{

  val belgianTvRef = context.actorOf(Props[BelgianTvActor].withRouter(RoundRobinRouter(2)), name = "BelgianTV")

  // TODO check the LoggingReceive docs and see if we can enable it on heroku
  def receive: Receive = LoggingReceive {
    case Start =>
      Logger.info("[" + this + "] - Received [start] from " + sender)
      val today = new DateMidnight()
      for(day <- 0 until 7){
        belgianTvRef ! FetchHumo(today.plusDays(day))
      }
    case StartTomatoes =>
      for{
        broadcasts <- Broadcast.findMissingTomatoes()
      }yield{
        broadcasts.foreach(belgianTvRef ! LinkTomatoes(_))
      }
  }
}