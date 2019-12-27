package services.actors

import akka.actor.{Actor, Props, Scheduler}

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger
import services.HumoReader
import models.Channel

object HumoActor {
  def props(humoReader: HumoReader) = Props(new HumoActor(humoReader))
}

class HumoActor(humoReader: HumoReader) extends Actor with LoggingActor {

  val logger = Logger("application.actor.humo")

  override def receive: Receive = {
    case FetchHumo(day, recipient) =>
      // copy the sender as we will reference it in a different thread
      val senderCopy = sender()
      logger.info(s"[$this] - Received fetch [$day] from ${sender()}, will reply to ${recipient}")

      implicit val scheduler: Scheduler = context.system.scheduler

      val f = humoReader.fetchDayRetryOnGatewayTimeout(day, Channel.channelFilter)
      f.onComplete { maybeHumoEvents =>
        recipient ! FetchHumoResult(day, maybeHumoEvents)
      }
  }

}
