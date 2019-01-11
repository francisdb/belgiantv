package services.actors

import java.time.LocalDate

import models.Channel
import play.api.Logger
import _root_.akka.actor.{Actor, Props}

import scala.util.Failure
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import _root_.akka.contrib.throttle.TimerBasedThrottler
import _root_.akka.contrib.throttle.Throttler._
import models._
import services._

object BelgianTvActor{
  def props(
    broadcastRepository: BroadcastRepository,
    movieRepository: MovieRepository,
    humoReader: HumoReader,
    yeloReader: YeloReader,
    belgacomReader: BelgacomReader,
    imdbApiService: OmdbApiService,
    tmdbApiService: TmdbApiService,
    tomatoesApiService: TomatoesApiService,
    tomatoesConfig: TomatoesConfig) =

    Props(new BelgianTvActor(broadcastRepository, movieRepository, humoReader, yeloReader, belgacomReader, imdbApiService, tmdbApiService, tomatoesApiService, tomatoesConfig))
}

class BelgianTvActor(
  broadcastRepository: BroadcastRepository,
  movieRepository: MovieRepository,
  humoReader: HumoReader,
  yeloReader: YeloReader,
  belgacomReader: BelgacomReader,
  imdbApiService: OmdbApiService,
  tmdbApiService: TmdbApiService,
  tomatoesApiService: TomatoesApiService,
  tomatoesConfig: TomatoesConfig) extends Actor with LoggingActor{
  
  val logger = Logger("application.actor")

  private val tomatoesRef = context.actorOf(TomatoesActor.props(tomatoesApiService), name = "Tomatoes")
  private val tomatoesThrottler = context.actorOf(Props(new TimerBasedThrottler(2 msgsPer 1.second)), name = "TomatoesThrottler")
  tomatoesThrottler ! SetTarget(Some(tomatoesRef))

  private val humoRef = context.actorOf(HumoActor.props(humoReader), name = "Humo")
  private val humoThrottler = context.actorOf(Props(new TimerBasedThrottler(1 msgsPer 5.second)), name = "HumoThrottler")
  humoThrottler ! SetTarget(Some(humoRef))
  
  override def preRestart(reason: Throwable, message: Option[Any]) {
    logger.error(s"Restarting due to [${reason.getMessage}}] when processing [${message.getOrElse("")}]", reason)
  }

  def receive: Receive = {

    case msg: LinkTmdb =>
      logger.info(s"[$this] - Received [$msg] from ${sender()}")

      val tmdbMovie = tmdbApiService.find(msg.broadcast.name, msg.broadcast.year)
      tmdbMovie.onComplete{
        case Success(Some(m)) =>
          broadcastRepository.setTmdb(msg.broadcast, m.id.toString, m.fixedAverage.map(_.toString))
          m.posterUrl.foreach(
            broadcastRepository.setTmdbImg(msg.broadcast, _)
          )
        case Success(None) =>
          logger.warn("No TMDb movie found for %s (%s)".format(msg.broadcast.name, msg.broadcast.year))
        case Failure(e) =>
          logger.error(e.getMessage, e)
      }

    case msg: LinkImdb =>
      logger.info(s"[$this] - Received [$msg] from ${sender()}")

      movieRepository.find(msg.broadcast.name, msg.broadcast.year).onComplete{
        case Failure(e) =>
          logger.error("Failed to find imdb: " + e.getMessage, e)
        case Success(movie) =>
          val movie2 = movie.orElse {
            // FIXME this is blocking
            val movie = Await.result(imdbApiService.find(msg.broadcast.name, msg.broadcast.year), 30.seconds)

            movie.map { m =>
              val dbMovie = new Movie(None, m.title, m.id, m.rating, m.year, m.poster)
              val created = Await.result(movieRepository.create(dbMovie), 30.seconds)
              created
            }
          }

          movie2 match{
            case Some(m) => broadcastRepository.setImdb(msg.broadcast, m.imdbId)
            case None    => logger.warn("No IMDB movie found for %s (%s)".format(msg.broadcast.name, msg.broadcast.year))
          }
      }

    case msg: LinkTomatoes =>
      logger.info(s"[$this] - Received [$msg] from ${sender()}")
      tomatoesThrottler ! FetchTomatoes(msg.broadcast.name, msg.broadcast.year, msg.broadcast.id.get)

    case msg: FetchTomatoesResult =>
      logger.info(s"[$this] - Received [$msg] from ${sender()}")
      val mergedScore = (msg.tomatoesMovie.ratings.audience_score + msg.tomatoesMovie.ratings.critics_score) / 2
      val scoreOpt = Option(mergedScore).filter(_ != 0)
      broadcastRepository.setTomatoes(msg.broadcastId, msg.tomatoesMovie.id.toString, scoreOpt.map(_.toString))

    case msg: FetchHumo =>
      logger.info(s"[$this] - Received [$msg] from ${sender()}")
      humoThrottler ! msg

    case msg: FetchHumoResult =>
      msg.events match {
        case Failure(e) =>
          logger.error(s"Failed to read humo day: ${e.getMessage}", e)
        case Success(humoEvents) =>
          val broadcasts:Future[Seq[Broadcast]] = Future.traverse(humoEvents){ event =>
            val broadcast = new Broadcast(
              None,
              event.title,
              event.channel.toLowerCase,
              event.toDateTime,
              event.year,
              humoId = Some(event.id),
              humoUrl = Some(event.url))
            broadcastRepository.findByDateTimeAndChannel(broadcast.datetime, broadcast.channel).map{
              case Some(existingBroadcast) =>
                if (existingBroadcast.imdbId.isEmpty) {
                  self ! LinkImdb(existingBroadcast)
                }
                existingBroadcast
              case None =>
                val saved = broadcastRepository.create(broadcast)
                self ! LinkImdb(saved)
                self ! LinkTmdb(saved)
                if(tomatoesConfig.isApiEnabled) {
                  self ! LinkTomatoes(saved)
                }
                saved

            }
          }

          broadcasts.onComplete{
            case Failure(e) =>
              logger.error(s"Failed to find broadcasts for humo events: ${e.getMessage}", e)
            case Success(foundBroadcast) =>
              self ! FetchYelo(msg.day, foundBroadcast)
              self ! FetchBelgacom(msg.day, foundBroadcast)
          }
      }

    case msg: FetchYelo =>
      logger.info(s"[$this] - Received [$msg] from ${sender()}")

      yeloReader.fetchDay(msg.day, Channel.channelFilter).onComplete {
        case Failure(e) =>
          logger.error(s"Failed to read yelo day: ${msg.day} - ${e.getMessage}", e)
        case Success(movies) =>
          // all unique channels
          // println(movies.groupBy{_.channel}.map{_._1})
          msg.events.foreach { broadcast =>
            if (broadcast.yeloUrl.isEmpty) {
              val found = movies.find(yeloMovie =>
                Channel.unify(yeloMovie.channel).toLowerCase == broadcast.channel.toLowerCase
                  && yeloMovie.startDateTime.toEpochMilli == broadcast.datetime.toEpochMilli
              )
              found.map { event =>
                broadcastRepository.setYelo(broadcast, event.id.toString, event.url)
              }.getOrElse {
                logger.warn("No yelo match for " + broadcast.channel + " " + broadcast.humanDate + " " + broadcast.name)
              }
            }
          }
      }

    case msg: FetchBelgacom =>
      logger.info(s"[$this] - Received [$msg] from ${sender()}")

      belgacomReader.searchMovies(msg.day).onComplete{
        case Failure(e) =>
          logger.error("Failed to read belgacom day: " + e.getMessage, e)
        case Success(movies) =>
          msg.events.foreach { broadcast =>
            if (broadcast.belgacomUrl.isEmpty) {
              val found = movies.find{ belgacomMovie =>
                Channel.unify(belgacomMovie.channel.name).toLowerCase == broadcast.channel.toLowerCase &&
                  belgacomMovie.program.toDateTime.toEpochMilli == broadcast.datetime.toEpochMilli
              }
              found.map { event =>
                broadcastRepository.setBelgacom(broadcast, event.program.programReferenceNumber, event.program.detailUrl)
              }.getOrElse {
                logger.warn("No belgacom match for " + broadcast.channel + " " + broadcast.humanDate + " " + broadcast.name)
              }
            }
          }
      }
  }
}



