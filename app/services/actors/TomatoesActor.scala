package services.actors

import akka.actor.{Actor, Props}
import play.api.Logger
import services.tomatoes.TomatoesApiService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object TomatoesActor{
  def props(tomatoesApiService: TomatoesApiService) = Props(new TomatoesActor(tomatoesApiService))
}

/**
 * Makes sure we only have 10	Calls per second
 */
class TomatoesActor(tomatoesApiService: TomatoesApiService) extends Actor with LoggingActor{

  val logger = Logger("application.actor.tomatoes")

  override def receive: Receive = {
    case msg:FetchTomatoes =>
      logger.info(s"[$this] - Received [$msg] from $sender")

      // copy the sender as we will reference it in a different thread
      val senderCopy = sender

      tomatoesApiService.find(msg.title, msg.year).onComplete{
        case Failure(e) =>
          logger.error("Failed to find tomatoes: " + e.getMessage, e)
        case Success(tomatoesMovie) =>
          tomatoesMovie match {
            case Some( m) =>
              senderCopy ! FetchTomatoesResult(m, msg.broadcastId)
            case None =>
              logger.warn("No Tomatoes movie found for %s (%s)".format(msg.title, msg.year))
          }
      }
  }
}
