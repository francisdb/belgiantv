package services.actors

import akka.actor.Actor
import play.api.Logger

trait LoggingActor extends Actor{

  override def unhandled(message: Any): Unit = {
    Logger.warn("[" + this + "] - Received unknown message [" + message + "] from " + sender)
    super.unhandled(message)
  }

  override def postRestart(reason: Throwable) {
    Logger.error("Actor restarted: " + reason.getMessage, reason)
    super.postRestart(reason)
  }

}
