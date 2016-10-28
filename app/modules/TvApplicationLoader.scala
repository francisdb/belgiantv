package modules

import controllers.Assets
import global.{ErrorReportingHttpErrorHandler, LoggingFilter}
import models.{BroadcastRepository, MovieRepository}
import org.webjars.play.WebJarComponents
import play.api._
import play.api.ApplicationLoader.Context
import play.api.http.HttpErrorHandler
import play.api.libs.mailer.MailerComponents
import play.modules.reactivemongo._
import services._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.ahc.AhcWSComponents

class TvApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }
    new MyComponents(context).application
  }
}

class MyComponents(context: Context)
  extends ReactiveMongoApiFromContext(context)// BuiltInComponentsFromContext(context)
  with WebJarComponents
  with MailerComponents
  with AhcWSComponents{


  lazy val mailer = new Mailer(mailerClient)
  lazy val broadcastRepository = new BroadcastRepository(reactiveMongoApi)
  lazy val movieRepository = new MovieRepository(reactiveMongoApi)

  lazy val humoReader = new HumoReader(wsApi)
  lazy val yeloReader = new YeloReader(wsApi)
  lazy val belgacomReader = new BelgacomReader(wsApi)
  lazy val imdbApiService = new ImdbApiService(wsApi)
  lazy val tmdbApiService = new TmdbApiService(wsApi)
  lazy val tomatoesApiService = new TomatoesApiService(wsApi)

  lazy val applicationController = new controllers.Application(
    actorSystem,
    reactiveMongoApi,
    broadcastRepository,
    movieRepository,
    mailer,
    webJarAssets,
    humoReader,
    yeloReader,
    belgacomReader,
    imdbApiService,
    tmdbApiService,
    tomatoesApiService
  )

  override lazy val httpErrorHandler: HttpErrorHandler =
    new ErrorReportingHttpErrorHandler(mailer, environment, configuration, sourceMapper, Some(router))

  lazy val loggingFilter = new LoggingFilter()
  override lazy val httpFilters = Seq(loggingFilter)


  lazy val assets = new Assets(httpErrorHandler)
  lazy val router = new _root_.router.Routes(httpErrorHandler, applicationController, webJarAssets, assets)
}
