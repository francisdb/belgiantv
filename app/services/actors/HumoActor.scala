package services.actors

import akka.actor.Props

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Logger

import services.{Mailer, HumoReader}
import models.Channel

object HumoActor{
  def props(mailer: Mailer, humoReader: HumoReader) = Props(classOf[HumoActor], mailer, humoReader)
}

class HumoActor(var mailer: Mailer, humoReader: HumoReader) extends MailingActor with LoggingActor{

  val logger = Logger("application.actor.humo")

  override def receive: Receive = {
    case msg:FetchHumo =>
      // copy the sender as we will reference it in a different thread
      val senderCopy = sender()
      logger.info(s"[$this] - Received [$msg] from $senderCopy")

      implicit val scheduler = context.system.scheduler

      humoReader.fetchDayRetryOnGatewayTimeout(msg.day, Channel.channelFilter).onComplete { maybeHumoEvents =>
        senderCopy ! FetchHumoResult(msg.day, maybeHumoEvents)
      }
  }

}
