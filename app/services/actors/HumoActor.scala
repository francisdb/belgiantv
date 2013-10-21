package services.actors

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Logger

import services.HumoReader
import models.Channel

class HumoActor extends MailingActor with LoggingActor{

  val logger = Logger("application.actor.humo")

  override def receive: Receive = {
    case msg:FetchHumo => {
      logger.info(s"[$this] - Received [$msg] from $sender")

      // copy the sender as we will reference it in a different thread
      val senderCopy = sender
      implicit val scheduler = context.system.scheduler

      HumoReader.fetchDayRetryOnGatewayTimeout(msg.day, Channel.channelFilter).onComplete { maybeHumoEvents =>
        senderCopy ! FetchHumoResult(msg.day, maybeHumoEvents)
      }
    }
  }

}
