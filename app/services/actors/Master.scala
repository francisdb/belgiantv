package services.actors

import java.time.LocalDate

import _root_.akka.actor.{Actor, ActorLogging, Props}
import play.api.Logger
import models.{BroadcastRepository, MovieRepository}
import services._

import scala.concurrent.ExecutionContext.Implicits.global
import akka.event.LoggingReceive
import _root_.global.Globals
import akka.stream.Materializer
import services.omdb.OmdbApiService
import services.proximus.BelgacomReader
import services.tmdb.TmdbApiService
import services.tomatoes.{TomatoesApiService, TomatoesConfig}
import services.trakt.{TraktApiService, TraktConfig}
import services.yelo.YeloReader

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
    tomatoesConfig: TomatoesConfig,
    traktApiService: TraktApiService,
    traktConfig: TraktConfig,
    materializer: Materializer
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
      tomatoesConfig,
      traktApiService,
      traktConfig,
      materializer)
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
  tomatoesConfig: TomatoesConfig,
  traktApiService: TraktApiService,
  traktConfig: TraktConfig,
  materializer: Materializer) extends Actor with LoggingActor with ActorLogging{

  private val logger = Logger("application")

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
      tomatoesConfig,
      traktApiService,
      traktConfig,
      materializer)
    /*.withRouter(RoundRobinPool(2))*/, name = "BelgianTV")

  // TODO check the LoggingReceive docs and see if we can enable it on heroku
  def receive: Receive = LoggingReceive {
    case Start =>
      logger.info(s"[$this] - Received [start] from $sender")
      val today = LocalDate.now(Globals.timezone)
      for(day <- 0 until 7){
        belgianTvRef ! FetchDay(today.plusDays(day))
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
