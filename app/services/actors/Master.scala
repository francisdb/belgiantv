package services.actors

import _root_.akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import play.api.Logger
import org.joda.time.DateMidnight
import models.{BroadcastRepository, MovieRepository}
import services._

import scala.concurrent.ExecutionContext.Implicits.global
import akka.event.LoggingReceive

object Master{
  def props(
    broadcastRepository: BroadcastRepository,
    movieRepository: MovieRepository,
    humoReader: HumoReader,
    yeloReader: YeloReader,
    belgacomReader: BelgacomReader,
    imdbApiService: ImdbApiService,
    tmdbApiService: TmdbApiService,
    tomatoesApiService: TomatoesApiService
  )
  = Props(
    classOf[Master],
    broadcastRepository,
    movieRepository,
    humoReader,
    yeloReader,
    belgacomReader,
    imdbApiService,
    tmdbApiService,
    tomatoesApiService
  )
}

class Master(
  broadcastRepository: BroadcastRepository,
  movieRepository: MovieRepository,
  humoReader: HumoReader,
  yeloReader: YeloReader,
  belgacomReader: BelgacomReader,
  imdbApiService: ImdbApiService,
  tmdbApiService: TmdbApiService,
  tomatoesApiService: TomatoesApiService) extends Actor with LoggingActor with ActorLogging{

  val belgianTvRef = context
    .actorOf(BelgianTvActor.props(
      broadcastRepository,
      movieRepository,
      humoReader,
      yeloReader,
      belgacomReader,
      imdbApiService,
      tmdbApiService,
      tomatoesApiService)
    .withRouter(RoundRobinPool(2)), name = "BelgianTV")

  // TODO check the LoggingReceive docs and see if we can enable it on heroku
  def receive: Receive = LoggingReceive {
    case Start =>
      Logger.info("[" + this + "] - Received [start] from " + sender)
      val today = new DateMidnight()
      for(day <- 0 until 7){
        belgianTvRef ! FetchHumo(today.plusDays(day))
      }
    case StartTomatoes =>
      for{
        broadcasts <- broadcastRepository.findMissingTomatoes()
      }yield{
        broadcasts.foreach(belgianTvRef ! LinkTomatoes(_))
      }
  }
}