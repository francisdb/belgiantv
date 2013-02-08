package services.akka

import _root_.akka.actor.{Actor, Props}
import _root_.akka.routing.RoundRobinRouter
import play.api.Logger
import org.joda.time.DateMidnight
import akka.actor.ActorLogging

class Master extends Actor{

  val workerRouter = context.actorOf(Props[BelgianTvActor].withRouter(RoundRobinRouter(2)), name = "BelgianTV")

  def receive: Receive = {
    case Start => {
      Logger.info("[" + this + "] - Received [start] from " + sender)
      val today = new DateMidnight()
      workerRouter ! FetchHumo(today)
      workerRouter ! FetchHumo(today.plusDays(1))
      workerRouter ! FetchHumo(today.plusDays(2))
      workerRouter ! FetchHumo(today.plusDays(3))
      workerRouter ! FetchHumo(today.plusDays(4))
      workerRouter ! FetchHumo(today.plusDays(5))
      workerRouter ! FetchHumo(today.plusDays(6))
    }
    
    case x => {
      Logger.warn("[" + this + "] - Received unknown message [" + x + "] from " + sender)
    }
  }
}