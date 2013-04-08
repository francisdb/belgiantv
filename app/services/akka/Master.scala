package services.akka

import _root_.akka.actor.{Actor, Props}
import _root_.akka.routing.RoundRobinRouter
import play.api.Logger
import org.joda.time.DateMidnight
import akka.actor.ActorLogging
import models.Broadcast

import scala.concurrent.ExecutionContext.Implicits.global

class Master extends Actor{

  val belgianTvRef = context.actorOf(Props[BelgianTvActor].withRouter(RoundRobinRouter(2)), name = "BelgianTV")

  def receive: Receive = {
    case Start => {
      Logger.info("[" + this + "] - Received [start] from " + sender)
      val today = new DateMidnight()
      belgianTvRef ! FetchHumo(today)
      belgianTvRef ! FetchHumo(today.plusDays(1))
      belgianTvRef ! FetchHumo(today.plusDays(2))
      belgianTvRef ! FetchHumo(today.plusDays(3))
      belgianTvRef ! FetchHumo(today.plusDays(4))
      belgianTvRef ! FetchHumo(today.plusDays(5))
      belgianTvRef ! FetchHumo(today.plusDays(6))
    }
    case StartTomatoes => {
        for{
          broadcasts <- Broadcast.findMissingTomatoes()
        }yield{
          broadcasts.foreach(belgianTvRef ! LinkTomatoes(_))
        }
    }
    
    case x => {
      Logger.warn("[" + this + "] - Received unknown message [" + x + "] from " + sender)
    }
  }
}