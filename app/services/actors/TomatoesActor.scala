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
    case msg@FetchTomatoes(title, year, broadcastId, recipient) =>
      logger.info(s"[$this] - Received [$msg] from $sender")

      tomatoesApiService.find(title, year).onComplete{
        case Failure(e) =>
          logger.error("Failed to find tomatoes: " + e.getMessage, e)
        case Success(tomatoesMovie) =>
          tomatoesMovie match {
            case Some( m) =>
              recipient ! FetchTomatoesResult(m, broadcastId)
            case None =>
              logger.warn("No Tomatoes movie found for %s (%s)".format(title, year))
          }
      }
  }
}
