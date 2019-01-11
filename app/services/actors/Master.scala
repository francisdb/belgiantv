package services.actors

import java.time.LocalDate

import _root_.akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import play.api.Logger
import models.{BroadcastRepository, MovieRepository}
import services._

import scala.concurrent.ExecutionContext.Implicits.global
import akka.event.LoggingReceive

import _root_.global.Globals

object Master{
  def props(
    broadcastRepository: BroadcastRepository,
    movieRepository: MovieRepository,
    humoReader: HumoReader,
    yeloReader: YeloReader,
    belgacomReader: BelgacomReader,
    imdbApiService: OmdbApiService,
    tmdbApiService: TmdbApiService,
    tomatoesApiService: TomatoesApiService,
    tomatoesConfig: TomatoesConfig
  )
  = Props(
    new Master(
      broadcastRepository,
      movieRepository,
      humoReader,
      yeloReader,
      belgacomReader,
      imdbApiService,
      tmdbApiService,
      tomatoesApiService,
      tomatoesConfig)
  )
}

class Master(
  broadcastRepository: BroadcastRepository,
  movieRepository: MovieRepository,
  humoReader: HumoReader,
  yeloReader: YeloReader,
  belgacomReader: BelgacomReader,
  imdbApiService: OmdbApiService,
  tmdbApiService: TmdbApiService,
  tomatoesApiService: TomatoesApiService,
  tomatoesConfig: TomatoesConfig) extends Actor with LoggingActor with ActorLogging{

  val belgianTvRef = context
    .actorOf(BelgianTvActor.props(
      broadcastRepository,
      movieRepository,
      humoReader,
      yeloReader,
      belgacomReader,
      imdbApiService,
      tmdbApiService,
      tomatoesApiService,
      tomatoesConfig)
    .withRouter(RoundRobinPool(2)), name = "BelgianTV")

  // TODO check the LoggingReceive docs and see if we can enable it on heroku
  def receive: Receive = LoggingReceive {
    case Start =>
      Logger.info("[" + this + "] - Received [start] from " + sender)
      val today = LocalDate.now(Globals.timezone)
      for(day <- 0 until 7){
        belgianTvRef ! FetchHumo(today.plusDays(day))
      }
    case StartTomatoes =>
      if(tomatoesConfig.isApiEnabled){
        for{
          broadcasts <- broadcastRepository.findMissingTomatoes()
        }yield{
          broadcasts.foreach(belgianTvRef ! LinkTomatoes(_))
        }
      }
  }
}
