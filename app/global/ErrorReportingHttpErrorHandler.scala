package global

import javax.inject.Provider

import play.api._
import play.api.http._
import play.api.mvc.RequestHeader
import play.api.routing.Router
import play.core.SourceMapper

class ErrorReportingHttpErrorHandler( environment: Environment,
                                      configuration: Configuration,
                                      sourceMapper: Option[SourceMapper] = None,
                                      router: => Option[Router] = None) extends DefaultHttpErrorHandler(environment, configuration, sourceMapper, router){

  def this(environment: Environment, configuration: Configuration, sourceMapper: OptionalSourceMapper,
           router: Provider[Router]) =
    this(environment, configuration, sourceMapper.sourceMapper, Some(router.get))

  override def onServerError(request: RequestHeader, throwable: Throwable) = {
    if(environment.mode == Mode.Prod){
      Logger.error("Internal server error for " + request.path, throwable)
    }
    super.onServerError(request, throwable)
  }

}
