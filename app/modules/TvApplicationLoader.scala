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
import services.Mailer

import play.api.libs.concurrent.Execution.Implicits._

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
  with MailerComponents{


  lazy val mailer = new Mailer(mailerClient)
  lazy val broadcastRepository = new BroadcastRepository(reactiveMongoApi)
  lazy val movieRepository = new MovieRepository(reactiveMongoApi)


  lazy val applicationController = new controllers.Application(
    actorSystem,
    reactiveMongoApi,
    broadcastRepository,
    movieRepository,
    mailer,
    webJarAssets)

  override lazy val httpErrorHandler: HttpErrorHandler =
    new ErrorReportingHttpErrorHandler(mailer, environment, configuration, sourceMapper, Some(router))

  lazy val loggingFilter = new LoggingFilter()
  override lazy val httpFilters = Seq(loggingFilter)


  lazy val assets = new Assets(httpErrorHandler)
  lazy val router = new _root_.router.Routes(httpErrorHandler, applicationController, webJarAssets, assets)
}
