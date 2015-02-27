package services

import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WS
import play.api.Logger
import play.api.Play.current
import models.helper.ImdbApiMovie

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ImdbProtocol{
  implicit val imdbApiMovieReads = Json.reads[ImdbApiMovie]
}

object ImdbApiService {

  private val logger = Logger("application.imdb")


  def find(title: String, year: Option[Int] = None): Future[Option[ImdbApiMovie]] = {

    // TODO fallback to a search without year if no result was found for a search including the year

    //val url = "http://www.imdbapi.com/"
    val url = "http://www.omdbapi.com/"
    var request = WS.url(url).withQueryString("t" -> title)
    if (year.isDefined) {
      request = request.withQueryString("y" -> year.get.toString)
    }
    
    logger.info("Fetching " + request)
    
    request.get().flatMap{ response =>
	      val json = response.json
	      val error = (json \ "Error").asOpt[String]
	      if(error.isDefined){
	        val msg = "IMDB api internal error for %s %s: %s".format(title, year, error.get)
	        logger.warn(msg)
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