package services

import play.api.Logger

import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WS
import org.apache.commons.lang.StringEscapeUtils
import org.joda.time.DateMidnight
import org.joda.time.Interval
import models.helper.{BelgacomResults, BelgacomListing, BelgacomProgram}
import concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.http.Status
import play.api.Play.current

import scala.util.{Failure, Success}

object BelgacomProtocol{

  implicit val belgacomProgramFormat = Json.reads[BelgacomProgram]
  implicit val belgacomResulstFormat = Json.reads[BelgacomResults]
  implicit val belgacomListingFormat = Json.reads[BelgacomListing]
}


object BelgacomReader{
  private val BASE = "http://www.proximustv.be/zcommon/tv-everywhere"
    
  private val logger = Logger("application.belgacom")

  def readMovies(date: DateMidnight) = {
    val now = new DateMidnight
    val dayOffset = new Interval(now, date).toPeriod.toStandardDays.getDays
    searchMovies(date)
  }
  
  def searchMovies(date: DateMidnight): Future[List[BelgacomProgram]] = {
    val page = 1
    searchMovies(date, page).map{ result =>
      result.tvmovies.get.data.map(program => program.copy(title = StringEscapeUtils.unescapeHtml(program.title)))
    }
  }
  
  private def searchMovies(date: DateMidnight, page:Int): Future[BelgacomListing] = {
    val now = new DateMidnight
    val dayOffset = new Interval(now, date).toPeriod.toStandardDays.getDays
    
    val url = BASE + "/listing"

    //http://www.proximustv.be/zcommon/tv-everywhere/listing?
    //tvgrid[filter]:lg-all
    //tvgrid[day]:1
    //tvgrid[offset]:1
    //tvgrid[channel]:0
    //tvgrid[hourIdx]:14
    //tvgrid[filterExpanded]:false
    //cacheBuster:1425065203316

    // or

    //tvmovies[day]:2
    //tvmovies[page]:1
    //cacheBuster:1425067433950

    val qs = List(
      "tvmovies[day]"-> dayOffset.toString,
      "tvmovies[page]" -> page.toString,
      "new_lang" -> "nl"
    )
    WS.url(url).withQueryString(qs:_*).get().flatMap{ response =>
      response.status match {
        case Status.OK =>
          import BelgacomProtocol._
          Json.fromJson[BelgacomListing](response.json) match {
            case JsSuccess(listing, _) => Future.successful(listing)
            case JsError(errors) => Future.failed(new RuntimeException(s"Failed to parse $url response to JSON: $errors ${response.body.take(500)}"))
          }
        case other =>
          Future.failed(new RuntimeException(s"Got $other for $url - ${response.body}"))
      }
    }
  }

}