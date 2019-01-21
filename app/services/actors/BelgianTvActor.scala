package services.actors

import models.Channel
import play.api.Logger
import _root_.akka.actor.{Actor, Props}

import scala.util.Failure
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.NotUsed
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Sink, Source}
import models._
import services._
import services.omdb.OmdbApiService
import services.proximus.BelgacomReader
import services.tmdb.TmdbApiService
import services.tomatoes.{TomatoesApiService, TomatoesConfig}
import services.trakt.{TraktApiService, TraktConfig}
import services.yelo.YeloReader

import scala.concurrent.Future

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
    tomatoesConfig: TomatoesConfig,
    traktApiService: TraktApiService,
    traktConfig: TraktConfig,
    materializer: Materializer) =

    Props(
      new BelgianTvActor(
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
        materializer
      )
    )
}

class BelgianTvActor(
  broadcastRepository: BroadcastRepository,
  movieRepository: MovieRepository,
  humoReader: HumoReader,
  yeloReader: YeloReader,
  belgacomReader: BelgacomReader,
  omdbApiService: OmdbApiService,
  tmdbApiService: TmdbApiService,
  tomatoesApiService: TomatoesApiService,
  tomatoesConfig: TomatoesConfig,
  traktApiService: TraktApiService,
  traktConfig: TraktConfig,
  materializer: Materializer) extends Actor with LoggingActor{

  private implicit val mat: Materializer = materializer

  val logger = Logger("application.actor")

  private val tomatoesThrottler = {
    logger.info("Starting tomatoes throttler")
    val tomatoesRef = context.actorOf(TomatoesActor.props(tomatoesApiService), name = "Tomatoes")
    Source.actorRef(bufferSize = 1000, OverflowStrategy.fail)
      .throttle(2, 1.second)
      .watchTermination()(loggingWatcher("Tomatoes"))
      .to(Sink.actorRef(tomatoesRef, NotUsed))
      .run()
  }

  private val humoThrottler = {
    logger.info("Starting humo throttler")
    val humoRef = context.actorOf(HumoActor.props(humoReader), name = "Humo")
    Source.actorRef(bufferSize = 1000, OverflowStrategy.fail)
      .throttle(1, 5.seconds)
      .watchTermination()(loggingWatcher("Humo"))
      .to(Sink.actorRef(humoRef, NotUsed))
      .run()
  }

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
      val res = movieRepository.find(msg.broadcast.name, msg.broadcast.year).flatMap {
        case Some(m) => Future.successful(Some(m))
        case None =>
          omdbApiService.find(msg.broadcast.name, msg.broadcast.year).flatMap {
            case Some(m) =>
              val dbMovie = new Movie(None, m.title, m.id, m.rating, m.year, m.poster)
              movieRepository.create(dbMovie).map(Some.apply)
            case None =>
              Future.successful(None)
          }
      }.flatMap{
        case Some(m) =>
          broadcastRepository.setImdb(msg.broadcast, m.imdbId)
        case None =>
          logger.warn(s"No IMDB movie found for ${msg.broadcast.name} (${msg.broadcast.year})")
          Future.unit
      }
      res.failed.foreach{ e =>
          logger.error(s"Error during imdb lookup: ${e.getMessage}", e)

      }

    case msg: LinkTomatoes =>
      logger.info(s"[$this] - Received [$msg] from ${sender()}")
      tomatoesThrottler ! FetchTomatoes(msg.broadcast.name, msg.broadcast.year, msg.broadcast.id.get, self)

    case msg: LinkTrakt =>
      logger.info(s"[$this] - Received [$msg] from ${sender()}")
      val result = traktApiService.find(msg.broadcast.name, msg.broadcast.year).flatMap {
        case Some(tm) =>
          traktApiService.movieRating(tm.ids.trakt).map{ rating =>
            broadcastRepository.setTrakt(msg.broadcast, tm.ids.trakt, tm.ids.slug, rating)
          }
        case None =>
          logger.warn("No Trakt movie found for %s (%s)".format(msg.broadcast.name, msg.broadcast.year))
          Future.unit
      }
      result.onComplete{
        case Failure(e) =>
          logger.error(s"Failed to link trakt for ${msg.broadcast.name} ${msg.broadcast.year}: ${e.getMessage}", e)
        case Success(_) =>
          logger.debug(s"Linked trakt for ${msg.broadcast.name} ${msg.broadcast.year}")
      }

    case msg: FetchTomatoesResult =>
      logger.info(s"[$this] - Received [$msg] from ${sender()}")
      val mergedScore = (msg.tomatoesMovie.ratings.audience_score + msg.tomatoesMovie.ratings.critics_score) / 2
      val scoreOpt = Option(mergedScore).filter(_ != 0)
      broadcastRepository.setTomatoes(msg.broadcastId, msg.tomatoesMovie.id.toString, scoreOpt.map(_.toString))

    case msg: FetchDay =>
      logger.info(s"[$this] - Received [$msg] from ${sender()}")
      humoThrottler ! FetchHumo(msg.day, self)

    case msg: FetchHumoResult =>
      msg.events match {
        case Failure(e) =>
          logger.error(s"Failed to read humo day: ${e.getMessage}", e)
        case Success(humoEvents) =>
          val broadcasts: Future[Seq[Broadcast]] = Future.traverse(humoEvents){ event =>
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
                if(traktConfig.isApiEnabled){
                  self ! LinkTrakt(saved)
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

  private def loggingWatcher[M,T](what: String)(m:M, f:Future[T]): M ={
    f.onComplete{
      case Success(_) => logger.info(s"$what throttler terminated")
      case Failure(e) => logger.error(s"$what throttler terminated with error", e)
    }
    m
  }
}



