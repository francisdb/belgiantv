package services.actors

import akka.actor.Props

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Logger

import services.{Mailer, HumoReader}
import models.Channel

object HumoActor{
  def props(mailer: Mailer) = Props(classOf[HumoActor], mailer)
}

class HumoActor(var mailer: Mailer) extends MailingActor with LoggingActor{

  val logger = Logger("application.actor.humo")

  override def receive: Receive = {
    case msg:FetchHumo =>
      // copy the sender as we will reference it in a different thread
      val senderCopy = sender()
      logger.info(s"[$this] - Received [$msg] from $senderCopy")

      implicit val scheduler = context.system.scheduler

      HumoReader.fetchDayRetryOnGatewayTimeout(msg.day, Channel.channelFilter).onComplete { maybeHumoEvents =>
        senderCopy ! FetchHumoResult(msg.day, maybeHumoEvents)
      }
  }

}
