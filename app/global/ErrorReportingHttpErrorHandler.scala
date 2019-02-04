package global

import javax.inject.Provider
import play.api._
import play.api.http._
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router
import play.core.SourceMapper

import scala.concurrent.Future

class ErrorReportingHttpErrorHandler( environment: Environment,
                                      configuration: Configuration,
                                      sourceMapper: Option[SourceMapper] = None,
                                      router: => Option[Router] = None) extends DefaultHttpErrorHandler(environment, configuration, sourceMapper, router){

  private final val logger = Logger("application")

  def this(environment: Environment, configuration: Configuration, sourceMapper: OptionalSourceMapper,
           router: Provider[Router]) =
    this(environment, configuration, sourceMapper.sourceMapper, Some(router.get))

  override def onServerError(request: RequestHeader, throwable: Throwable): Future[Result] = {
    if(environment.mode == Mode.Prod){
      logger.error(s"Internal server error for ${request.path}", throwable)
    }
    super.onServerError(request, throwable)
  }

}
