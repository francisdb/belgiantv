package global

import javax.inject.{Provider, Singleton, Inject}

import play.api._
import play.api.http._
import play.api.mvc.RequestHeader
import play.api.routing.Router
import play.core.SourceMapper
import services.Mailer

class ErrorReportingHttpErrorHandler( mailer: Mailer,
                                      environment: Environment,
                                      configuration: Configuration,
                                      sourceMapper: Option[SourceMapper] = None,
                                      router: => Option[Router] = None) extends DefaultHttpErrorHandler(environment, configuration, sourceMapper, router){

  def this(mailer: Mailer, environment: Environment, configuration: Configuration, sourceMapper: OptionalSourceMapper,
           router: Provider[Router]) =
    this(mailer, environment, configuration, sourceMapper.sourceMapper, Some(router.get))

  override def onServerError(request: RequestHeader, throwable: Throwable) = {
    if(environment.mode == Mode.Prod){
      Logger.error("Server error: " + request.path, throwable)
      val message = s"Internal server error: ${throwable.getMessage}"
      mailer.sendMail(message, throwable)
    }
    super.onServerError(request, throwable)
  }

}
