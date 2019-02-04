package services.actors

import akka.actor.Actor
import play.api.Logger

trait LoggingActor extends Actor{

  private val logger = Logger("application")

  override def unhandled(message: Any): Unit = {
    logger.warn(s"[$this] - Received unknown message [$message] from $sender")
    super.unhandled(message)
  }

  override def postRestart(reason: Throwable) {
    logger.error(s"Actor restarted: ${reason.getMessage}", reason)
    super.postRestart(reason)
  }

}
