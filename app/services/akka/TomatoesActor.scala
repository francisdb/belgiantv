package services.akka

import akka.actor.Actor
import play.api.Logger
import org.joda.time.DateMidnight
import models.Broadcast
import services.{ErrorReportingSupport, Mailer, TomatoesApiService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

/**
 * Makes sure we only have 10	Calls per second
 */
class TomatoesActor extends Actor with ErrorReportingSupport{

  val logger = Logger("application.actor.tomatoes")

  override def receive: Receive = {
    case msg:FetchTomatoes => {
      logger.info(s"[$this] - Received [$msg] from $sender")

      // copy the sender as we will reference it in a different thread
      val mySender = sender

      val movie = TomatoesApiService.find(msg.title, msg.year).onComplete{
        case Failure(e) =>
          reportFailure("Failed to find tomatoes: " + e.getMessage, e)
        case Success(tomatoesMovie) =>
          tomatoesMovie.map(
            m => {
              mySender ! FetchTomatoesResult(m, msg.broadcastId)
            }
          ).getOrElse{
            logger.warn("No Tomatoes movie found for %s (%s)".format(msg.title, msg.year))
          }
      }
    }
    case x => {
      Logger.warn("[" + this + "] - Received unknown message [" + x + "] from " + sender)
    }
  }
}
