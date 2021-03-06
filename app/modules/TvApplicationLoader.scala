package modules

import controllers.Assets
import global.{ErrorReportingHttpErrorHandler, LoggingFilter}
import models.{BroadcastRepository, MovieRepository}
import org.webjars.play.{RequireJS, WebJarComponents}
import play.api.ApplicationLoader.Context
import play.api.http.HttpErrorHandler
import play.modules.reactivemongo._
import services._
import play.api.libs.ws.ahc.AhcWSComponents
import controllers.AssetsComponents
import play.api.{ApplicationLoader, LoggerConfigurator}
import services.omdb.OmdbApiService
import services.proximus.BelgacomReader
import services.tmdb.TmdbApiService
import services.tomatoes.{TomatoesApiService, TomatoesConfig}
import services.trakt.{TraktApiService, TraktConfig}
import services.yelo.YeloReader

class TvApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }
    new MyComponents(context).application
  }
}

trait FixedWebjarComponents extends WebJarComponents {
  def httpErrorHandler: HttpErrorHandler

  lazy val errorHandler: HttpErrorHandler = httpErrorHandler

  // all this stuff for webjars?
  lazy val requireJs = new RequireJS(webJarsUtil)
  lazy val webjarsRoutes: webjars.Routes = new webjars.Routes(
    errorHandler,
    requireJs,
    webJarAssets
  )
}

class MyComponents(context: Context)
    extends ReactiveMongoApiFromContext(context) // BuiltInComponentsFromContext(context)
    with FixedWebjarComponents
    with AhcWSComponents
    with AssetsComponents {

  lazy val broadcastRepository = new BroadcastRepository(reactiveMongoApi, executionContext)
  lazy val movieRepository     = new MovieRepository(reactiveMongoApi, executionContext)

  lazy val humoReader         = new HumoReader(wsClient)
  lazy val yeloReader         = new YeloReader(wsClient)
  lazy val belgacomReader     = new BelgacomReader(wsClient)
  lazy val imdbApiService     = new OmdbApiService(wsClient)
  lazy val tmdbApiService     = new TmdbApiService(wsClient)
  lazy val tomatoesConfig     = new TomatoesConfig(configuration)
  lazy val tomatoesApiService = new TomatoesApiService(tomatoesConfig, wsClient)
  lazy val traktConfig        = new TraktConfig(configuration)
  lazy val traktApiService    = new TraktApiService(traktConfig, wsClient)

  lazy val applicationController = new controllers.Application(
    controllerComponents,
    actorSystem,
    reactiveMongoApi,
    broadcastRepository,
    movieRepository,
    webJarsUtil,
    humoReader,
    yeloReader,
    belgacomReader,
    imdbApiService,
    tmdbApiService,
    tomatoesApiService,
    tomatoesConfig,
    traktApiService,
    traktConfig
  )

  override lazy val httpErrorHandler: HttpErrorHandler =
    new ErrorReportingHttpErrorHandler(environment, configuration, devContext.map(_.sourceMapper), Some(router))

  lazy val loggingFilter        = new LoggingFilter()
  override lazy val httpFilters = Seq(loggingFilter)

  override lazy val assets = new Assets(errorHandler, assetsMetadata)

  lazy val router = new _root_.router.Routes(httpErrorHandler, applicationController, webjarsRoutes, assets)
}
