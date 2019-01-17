package services.tmdb

import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WSClient
import services.PlayUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TmdbProtocol{
  implicit val tmdbImageReads = Json.reads[TmdbImage]
  implicit val tmdbPosterReads = Json.reads[TmdbPoster]
  implicit val tmdbBackDropReads = Json.reads[TmdbBackDrop]
  implicit val tmdbMovieReads = Json.reads[TmdbMovie]
  implicit val tmdbMovieSearchReads = Json.reads[TmdbMovieSearch]
  implicit val tmdbMovieSearchPagerReads = Json.reads[TmdbMovieSearchPager]
}

class TmdbApiService(ws: WSClient){
	
	val BASE = "http://api.themoviedb.org/3"
	  
	private val logger = Logger("application.tmdb")

	private lazy val apiKey = PlayUtil.config("tmdb.apikey")

	def find(title:String, year:Option[Int] = None): Future[Option[TmdbMovieSearch]] = {
		val result = search(title, year)
		// TODO fall back to title only search if none found?
		// create test to see if this is needed
		result.map(list => list.headOption)
	}
	
	
	def search(title:String, year:Option[Int] = None): Future[List[TmdbMovieSearch]] = {
	  //println(apiKey)
		val url = BASE + "/search/movie"
		
		val qs = List("api_key"-> apiKey, "query" -> title)
		val qs2 = year.map(y => qs.+:("year" -> y.toString)).getOrElse(qs)
		
		logger.info(url)
		val response = ws.url(url)
		  .addHttpHeaders("Accept" -> "application/json")
		  .addQueryStringParameters(qs2:_*).get()
		  
		response.flatMap{ request =>
		  logger.info(url + " " + request.status)
		  request.status match {
		    case 503 => 
		      logger.error(request.status + " " + request.statusText + " " + request.body)
		      Future.successful(List())
        case 401 =>
          logger.error(request.status + " " + request.statusText + " " + request.body)
          Future.successful(List())
		    case _ =>
          import TmdbProtocol._
		      Json.fromJson[TmdbMovieSearchPager](request.json) match {
            case JsSuccess(pager, _) => Future.successful(pager.results)
            case JsError(errors) => Future.failed(new RuntimeException(errors.toString()))
          }
		  }
	      
		}
	}
}
