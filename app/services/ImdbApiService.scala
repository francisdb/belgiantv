package services

import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WSAPI
import play.api.Logger
import models.helper.ImdbApiMovie

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

object ImdbProtocol{
  implicit val imdbApiMovieReads = Json.reads[ImdbApiMovie]
}

object ImdbApiService {

}

class ImdbApiService(ws: WSAPI){

  private val logger = Logger("application.imdb")


  def find(title: String, year: Option[Int] = None): Future[Option[ImdbApiMovie]] = {

    // TODO fallback to a search without year if no result was found for a search including the year

    //val url = "http://www.imdbapi.com/"
    val url = "http://www.omdbapi.com/"
    val request = ws.url(url).withQueryString("t" -> title)
    val requestExtended = year.map(year =>
      request.withQueryString("y" -> year.toString)
    ).getOrElse(request)

    
    logger.info("Fetching " + requestExtended)

    requestExtended.get().map{ response =>
      val json = try{
        response.json
      }catch{
        case NonFatal(e) =>
          sys.error(s"Failed to parse $url response to JSON: ${e.getMessage} ${response.body.take(500)}")
      }
      val errorOpt = (json \ "Error").asOpt[String]
      errorOpt match {
        case None =>
          import ImdbProtocol._
          Json.fromJson[ImdbApiMovie](response.json) match {
            case JsSuccess(movie, _) => Option(movie)
            case JsError(errors) => sys.error(errors.toString())
          }
        case Some(error) =>
          logger.warn(s"IMDB api internal error for $title $year: $error")
          // TODO should we also fail the future here?
          None
      }
    }
  }

}