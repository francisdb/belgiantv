package models

import util.{Failure, Success, Try}
import reactivemongo.core.commands.LastError
import play.api.Logger

trait MongoSupport {

  protected val logger:Logger

  protected def mongoLogger(le:Try[LastError], successMessage:String) = {
    le match {
      case Success(le) =>
        if (le.inError) {
          logger.error(le.message)
        } else {
          logger.debug(successMessage)
        }
      case Failure(ex) => logger.error(ex.getMessage, ex)
    }
  }
}
