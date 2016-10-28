package services.actors

import akka.actor.{Props, Actor}
import play.api.Logger
import org.joda.time.DateMidnight
import models.Broadcast
import services.{ErrorReportingSupport, Mailer, TomatoesApiService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

object TomatoesActor{
  def props(mailer: Mailer, tomatoesApiService: TomatoesApiService) = Props(classOf[TomatoesActor], mailer, tomatoesApiService)
}

/**
 * Makes sure we only have 10	Calls per second
 */
class TomatoesActor(val mailer: Mailer, tomatoesApiService: TomatoesApiService) extends MailingActor with ErrorReportingSupport with LoggingActor{

  val logger = Logger("application.actor.tomatoes")

  override def receive: Receive = {
    case msg:FetchTomatoes =>
      logger.info(s"[$this] - Received [$msg] from $sender")

      // copy the sender as we will reference it in a different thread
      val senderCopy = sender

      tomatoesApiService.find(msg.title, msg.year).onComplete{
        case Failure(e) =>
          reportFailure("Failed to find tomatoes: " + e.getMessage, e)
        case Success(tomatoesMovie) =>
          tomatoesMovie.map( m =>
            senderCopy ! FetchTomatoesResult(m, msg.broadcastId)
          ).getOrElse{
            logger.warn("No Tomatoes movie found for %s (%s)".format(msg.title, msg.year))
          }
      }
  }
}
