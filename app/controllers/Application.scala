package controllers

import _root_.models.helper.BroadcastInfo
import akka.actor.ActorSystem
import models.{Broadcast, BroadcastRepository, Movie, MovieRepository}
import org.joda.time.{DateMidnight, DateTime, Interval}
import org.webjars.play.WebJarsUtil
import play.api.Logger
import play.api.mvc._
import services._
import services.actors.{Master, Start, StartTomatoes}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.{ExecutionContext, Future}

class Application(
  components: ControllerComponents,
  actorSystem: ActorSystem,
  val reactiveMongoApi: ReactiveMongoApi,
  broadcastRepository: BroadcastRepository,
  movieRepository: MovieRepository,
  webJarsUtil: WebJarsUtil,
  humoReader: HumoReader,
  yeloReader: YeloReader,
  belgacomReader: BelgacomReader,
  imdbApiService: OmdbApiService,
  tmdbApiService: TmdbApiService,
  tomatoesApiService: TomatoesApiService
  )(implicit ec: ExecutionContext) extends AbstractController(components) with ReactiveMongoComponents {

  implicit val wu: WebJarsUtil = webJarsUtil

  Logger.info("Scheduling actor trigger")
  // TODO better location for the actor?
  // TODO create an actor module that is enabled in the application.conf
  private val masterActorRef = actorSystem.actorOf(
    Master.props(
      broadcastRepository,
      movieRepository,
      humoReader,
      yeloReader,
      belgacomReader,
      imdbApiService,
      tmdbApiService,
      tomatoesApiService
    ),
    name = "masterActor"
  )
  //Akka.system.scheduler.schedule(0 seconds, 12 hours, Application.masterActorRef, Start)

  //private implicit val wjAssets = webJarAssets

  def index = Action.async{ implicit request =>

    // anything that has started more than an hour ago is not interesting
    val start = new DateTime().minusHours(1)
    // for seven days in the future (midnight)
    val end = new DateMidnight().plusDays(7)
    val interval = new Interval(start, end)

    for{
      broadcasts <- broadcastRepository.findByInterval(interval)
      infos <- Future.traverse(broadcasts)(broadcast => linkWithMovie(broadcast))
    }yield{
      val sorted = infos.sortWith(BroadcastInfo.scoreSorter)
      Ok(views.html.index(sorted))
    }
  }

  def linkWithMovie(broadcast: Broadcast): Future[BroadcastInfo] = {
    implicit val reader = Movie.movieFormat//MovieBSONReader

    broadcast.imdbId match {
      case Some(imdbId) => movieRepository.findByImdbId(imdbId).map(BroadcastInfo(broadcast, _))
      case None => Future.successful(BroadcastInfo(broadcast, None))
    }
  }

  def scan = Action {
    masterActorRef ! Start
    Redirect(routes.Application.index())
      .flashing("message" -> "Started database update...")
  }

  def tomatoes = Action {
    masterActorRef ! StartTomatoes
    Redirect(routes.Application.index())
      .flashing("message" -> "Started tomatoes update...")
  }
  


}
