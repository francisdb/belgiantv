package services.actors

import akka.actor.Actor
import play.api.Logger
import services.Mailer

trait MailingActor extends Actor{

  private val LOGGER = Logger(getClass)

// TODO not sure how to handle this in scala
//  @Override
//  public void onReceive(Object message) throws Exception {
//    Logger.info(this.getClass().getSimpleName() + " received a message: " + message);
//  }

  override def postRestart(reason: Throwable) {
    LOGGER.info("Actor restarted: " + reason.getMessage())
    Mailer.sendMail(s"Actor ${this.getClass().getSimpleName()} restarted", reason)
    super.postRestart(reason)
  }

  override def unhandled(message: Any) {
    LOGGER.info("received unknown message: " + message)
    super.unhandled(message)
  }
}
