package services

import play.api.Logger

trait ErrorReportingSupport {

  protected val logger:Logger

  protected def reportFailure(message:String, e: Throwable) {
    logger.error(message, e)
    Mailer.sendMail(message, e)
  }

  protected def reportFailure(e: Throwable) {
    reportFailure(e.getMessage, e)
  }
}
