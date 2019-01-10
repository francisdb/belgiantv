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
import play.api.{ApplicationLoader, BuiltInComponents, LoggerConfigurator}

class TvApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }
    new MyComponents(context).application
  }
}


trait FixedWebjarComponents extends WebJarComponents{
  def httpErrorHandler: HttpErrorHandler

  lazy val errorHandler: HttpErrorHandler = httpErrorHandler

  // all this stuff for webjars?
  lazy val requireJs = new RequireJS()
  lazy val webjarsRoutes: webjars.Routes = new webjars.Routes(
    errorHandler, requireJs, webJarAssets
  )
}

class MyComponents(context: Context)
  extends ReactiveMongoApiFromContext(context)// BuiltInComponentsFromContext(context)
  with FixedWebjarComponents
  with AhcWSComponents
  with AssetsComponents{

  lazy val broadcastRepository = new BroadcastRepository(reactiveMongoApi)
  lazy val movieRepository = new MovieRepository(reactiveMongoApi)

  lazy val humoReader = new HumoReader(wsClient)
  lazy val yeloReader = new YeloReader(wsClient)
  lazy val belgacomReader = new BelgacomReader(wsClient)
  lazy val imdbApiService = new OmdbApiService(wsClient)
  lazy val tmdbApiService = new TmdbApiService(wsClient)
  lazy val tomatoesApiService = new TomatoesApiService(wsClient)

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
    tomatoesApiService
  )

  override lazy val httpErrorHandler: HttpErrorHandler =
    new ErrorReportingHttpErrorHandler(environment, configuration, sourceMapper, Some(router))


  lazy val loggingFilter = new LoggingFilter()
  override lazy val httpFilters = Seq(loggingFilter)

  override lazy val assets = new Assets(errorHandler, assetsMetadata)

  lazy val router = new _root_.router.Routes(httpErrorHandler, applicationController, webjarsRoutes, assets)
}
