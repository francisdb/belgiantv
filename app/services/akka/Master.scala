package services.akka

import _root_.akka.actor.{Actor, Props}
import _root_.akka.routing.RoundRobinRouter
import play.api.Logger
import org.joda.time.DateMidnight
import akka.actor.ActorLogging

class Master extends Actor{

  val workerRouter = context.actorOf(Props[BelgianTvActor].withRouter(RoundRobinRouter(10)), name = "BelgianTV")

  protected def receive: Receive = {
    case Start => {
      Logger.info("[" + this + "] - Received [start] from " + sender)
      val today = new DateMidnight()
      val msgOut = FetchHumo(today)
      workerRouter ! msgOut
    }
    
    case _ => {
      Logger.warn("[" + this + "] - Received unknown message from " + sender)
    }
  }
}