package models

import reactivemongo.api.commands.WriteResult

import scala.util.{Try, Success, Failure}
import reactivemongo.core.commands.LastError
import play.api.Logger
import services.{ErrorReportingSupport, Mailer}

trait MongoSupport extends ErrorReportingSupport{

  protected val logger:Logger

  protected def mongoLogger(lastError:Try[WriteResult], successMessage:String) = {
    lastError match {
      case Success(le) =>
        if (le.inError) {
          logger.error(le.message)
        } else {
          logger.debug(successMessage)
        }
      case Failure(ex) =>
        reportFailure(ex)
    }
  }
}
