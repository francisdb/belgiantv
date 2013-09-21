package services.akka

import services.{HumoReader, ErrorReportingSupport}
import play.api.Logger
import models.{Broadcast, Channel}
import akka.actor.Actor

import scala.concurrent.ExecutionContext.Implicits.global


class HumoActor extends Actor with ErrorReportingSupport{

  val logger = Logger("application.actor.humo")

  override def receive: Receive = {
    case msg:FetchHumo => {
      logger.info(s"[$this] - Received [$msg] from $sender")

      // copy the sender as we will reference it in a different thread
      val mySender = sender

      HumoReader.fetchDayRetry(msg.day, Channel.channelFilter).onComplete { maybeHumoEvents =>
        mySender ! FetchHumoResult(msg.day, maybeHumoEvents)
      }
    }
    case x => {
      Logger.warn("[" + this + "] - Received unknown message [" + x + "] from " + sender)
    }
  }

}
