package services.actors

import models.Channel
import play.api.Logger
import _root_.akka.actor.Props

import org.joda.time.DateTimeZone

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
    mailer: Mailer,
    humoReader: HumoReader,
    yeloReader: YeloReader,
    belgacomReader: BelgacomReader,
    imdbApiService: ImdbApiService,
    tmdbApiService: TmdbApiService,
    tomatoesApiService: TomatoesApiService) =

    Props(
      classOf[BelgianTvActor],
      broadcastRepository,
      movieRepository,
      mailer,
      humoReader,
      yeloReader,
      belgacomReader,
      imdbApiService,
      tmdbApiService,
      tomatoesApiService
    )
}

class BelgianTvActor(
  broadcastRepository: BroadcastRepository,
  movieRepository: MovieRepository,
  val mailer: Mailer,
  humoReader: HumoReader,
  yeloReader: YeloReader,
  belgacomReader: BelgacomReader,
  imdbApiService: ImdbApiService,
  tmdbApiService: TmdbApiService,
  tomatoesApiService: TomatoesApiService) extends MailingActor with ErrorReportingSupport with LoggingActor{
  
  val logger = Logger("application.actor")

  val tomatoesRef = context.actorOf(TomatoesActor.props(mailer, tomatoesApiService), name = "Tomatoes")
  val tomatoesThrottler = context.actorOf(Props(new TimerBasedThrottler(2 msgsPer 1.second)), name = "TomatoesThrottler")
  tomatoesThrottler ! SetTarget(Some(tomatoesRef))

  val humoRef = context.actorOf(HumoActor.props(mailer, humoReader), name = "Humo")
  val humoThrottler = context.actorOf(Props(new TimerBasedThrottler(1 msgsPer 5.second)), name = "HumoThrottler")
  humoThrottler ! SetTarget(Some(humoRef))
  
  override def preRestart(reason: Throwable, message: Option[Any]) {
    logger.error(s"Restarting due to [${reason.getMessage}}] when processing [${message.getOrElse("")}]", reason)
  }

  def receive: Receive = {

    case msg: LinkTmdb =>
      logger.info(s"[$this] - Received [$msg] from ${sender()}")

      val tmdbmovie = tmdbApiService.find(msg.broadcast.name, msg.broadcast.year)

      tmdbmovie.map { mOption =>
        mOption.map{ m =>
          broadcastRepository.setTmdb(msg.broadcast, m.id.toString, m.vote_average.toString)
           m.posterUrl.map(broadcastRepository.setTmdbImg(msg.broadcast, _))
        }.getOrElse{
        	logger.warn("No TMDb movie found for %s (%s)".format(msg.broadcast.name, msg.broadcast.year))
        	None
        }
      }.onFailure{
        case e: Exception =>
          logger.error(e.getMessage, e)
      }

    case msg: LinkImdb =>
      logger.info(s"[$this] - Received [$msg] from ${sender()}")

      movieRepository.find(msg.broadcast.name, msg.broadcast.year).onComplete{
        case Failure(e) =>
          reportFailure("Failed to find imdb: " + e.getMessage, e)
        case Success(movie) =>
          val movie2 = movie.orElse {
            // FIXME this is blocking
            val movie = Await.result(imdbApiService.find(msg.broadcast.name, msg.broadcast.year), 30.seconds)

            movie.map { m =>
              val dbMovie = new Movie(None, m.title, m.id, m.rating, m.year, m.poster)
              val created = movieRepository.create(dbMovie)
              created
            }
          }

          movie2 match{
            case Some(m:Movie) => broadcastRepository.setImdb(msg.broadcast, m.imdbId)
            case None => logger.warn("No IMDB movie found for %s (%s)".format(msg.broadcast.name, msg.broadcast.year))
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
          reportFailure("Failed to read humo day: " + e.getMessage, e)
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
                self ! LinkTomatoes(saved)
                saved

            }
          }

          broadcasts.onComplete{
            case Failure(e) =>
              reportFailure("Failed to find broadcasts for humo events: " + e.getMessage, e)
            case Success(foundBroadcast) =>
              self ! FetchYelo(msg.day, foundBroadcast)
              self ! FetchBelgacom(msg.day, foundBroadcast)
          }
      }

    case msg: FetchYelo =>
      logger.info(s"[$this] - Received [$msg] from ${sender()}")

      yeloReader.fetchDay(msg.day, Channel.channelFilter).onComplete {
        case Failure(e) =>
          reportFailure("Failed to read yelo day: " + msg.day + " - " + e.getMessage, e)
        case Success(movies) =>
          // all unique channels
          // println(movies.groupBy{_.channel}.map{_._1})
          msg.events.foreach { broadcast =>
            if (broadcast.yeloUrl.isEmpty) {
              val found = movies.find(yeloMovie => Channel.unify(yeloMovie.channel).toLowerCase == broadcast.channel.toLowerCase && yeloMovie.startDateTime.withZone(DateTimeZone.UTC) == broadcast.datetime.withZone(DateTimeZone.UTC))
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

      belgacomReader.readMovies(msg.day).onComplete{
        case Failure(e) =>
          reportFailure("Failed to read belgacom day: " + e.getMessage, e)
        case Success(movies) =>
          msg.events.foreach { broadcast =>
            if (broadcast.belgacomUrl.isEmpty) {
              val found = movies.find{belgacomMovie =>
                Channel.unify(belgacomMovie.channelName).toLowerCase == broadcast.channel.toLowerCase && belgacomMovie.toDateTime.withZone(DateTimeZone.UTC) == broadcast.datetime.withZone(DateTimeZone.UTC)
              }
              found.map { event =>
                broadcastRepository.setBelgacom(broadcast, event.programId.toString, event.getProgramUrl)
              }.getOrElse {
                logger.warn("No belgacom match for " + broadcast.channel + " " + broadcast.humanDate + " " + broadcast.name)
              }
            }
          }
      }
  }
}



