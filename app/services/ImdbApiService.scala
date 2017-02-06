package services

import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WSAPI
import play.api.Logger
import models.helper.ImdbApiMovie

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

    requestExtended.get().flatMap{ response =>
      val json = response.json
      val error = (json \ "Error").asOpt[String]
      if(error.isDefined){
        logger.warn(s"IMDB api internal error for $title $year: ${error.get}")
        // TODO should we also fail the future here?
        Future.successful(None)
      }else{
        import ImdbProtocol._
        Json.fromJson[ImdbApiMovie](response.json) match {
          case JsSuccess(movie, _) => Future.successful(Option(movie))
          case JsError(errors) => Future.failed(new RuntimeException(errors.toString()))
        }
      }
    }
  }

}