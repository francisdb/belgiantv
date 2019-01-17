package services.omdb

import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WSClient
import services.PlayUtil

import scala.concurrent.Future
import scala.util.control.NonFatal

object OmdbProtocol{
  implicit val omdbApiMovieReads = Json.reads[ImdbApiMovie]
}

object OmdbApiService {

}

class OmdbApiService(ws: WSClient){


  import scala.concurrent.ExecutionContext.Implicits.global

  private val logger = Logger("application.omdb")

  private lazy val apiKey = PlayUtil.config("omdb.apikey")


  def find(title: String, year: Option[Int] = None): Future[Option[ImdbApiMovie]] = {

    // TODO fallback to a search without year if no result was found for a search including the year
    val url = "http://www.omdbapi.com/"
    val request = ws.url(url)
      .addQueryStringParameters("t" -> title)
      .addQueryStringParameters("apikey" -> apiKey)
    val requestExtended = year.map(year =>
      request.addQueryStringParameters("y" -> year.toString)
    ).getOrElse(request)


    logger.info("Fetching " + requestExtended)

    requestExtended.get().map{ response =>
      response.status match {
        case Status.OK =>
          val json = try{
            response.json
          }catch{
            case NonFatal(e) =>
              sys.error(s"Failed to parse $url response to JSON: ${e.getMessage} ${response.body.take(500)}")
          }
          // TODO does this actually still happen now that we have the status code check?
          val errorOpt = (json \ "Error").asOpt[String]
          errorOpt match {
            case None =>
              import OmdbProtocol._
              Json.fromJson[ImdbApiMovie](response.json) match {
                case JsSuccess(movie, _) => Option(movie)
                case JsError(errors) => sys.error(errors.toString())
              }
            case Some(error) =>
              logger.warn(s"Omdb api internal error for $title $year: $error")
              // TODO should we also fail the future here?
              None
          }
        case Status.NOT_FOUND =>
          None
        case otherStatus =>
          sys.error(s"Got $otherStatus for $url with body: ${response.body.take(500)}")
      }

    }
  }

}
