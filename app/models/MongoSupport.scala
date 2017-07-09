package models

import reactivemongo.api.commands.WriteResult

import scala.util.{Failure, Success, Try}
import play.api.Logger
import reactivemongo.api.FailoverStrategy
import scala.concurrent.duration._

trait MongoSupport{

  protected val logger:Logger

  protected val herokuMLabFailover = FailoverStrategy(100.milliseconds, 20, {n => n})

  // TODO how can we get this from the config / extract if from the mongo url
  protected val dbName = "heroku_app4877663"

  protected def mongoLogger(lastError:Try[WriteResult], successMessage:String) = {
    lastError match {
      case Success(result) =>
        result.writeConcernError.map{ e =>
          logger.error(e.errmsg)
        }.getOrElse{
          if(result.writeErrors.nonEmpty){
            logger.error(result.writeErrors.mkString("|"))
          }else{
            logger.debug(successMessage)
          }
        }
      case Failure(ex) =>
        logger.error(s"Mongo error: ${ex.getMessage}", ex)
    }
  }
}
