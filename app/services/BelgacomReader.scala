package services

import play.api.Logger

import com.fasterxml.jackson.core.JsonParseException
import play.api.libs.ws.WS
import org.apache.commons.lang.StringEscapeUtils
import org.joda.time.DateMidnight
import org.joda.time.Interval
import models.helper.BelgacomProgram
import models.helper.BelgacomListing
import concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.http.Status


object BelgacomReader extends JacksonMapper{
  private val BASE = "http://sphinx.skynet.be/tv-overal/ajax"
    
  private val logger = Logger("application.belgacom")

  def readMovies(date: DateMidnight) = {
    val now = new DateMidnight
    val dayOffset = new Interval(now, date).toPeriod.toStandardDays.getDays
    searchMovies(date)
  }
  
  def searchMovies(date: DateMidnight): Future[List[BelgacomProgram]] = {
    val page = 1
    searchMovies(date, page).map{ result =>
      result.tvmovies.data.map(program => program.copy(title = StringEscapeUtils.unescapeHtml(program.title)))
    }
  }
  
  private def searchMovies(date: DateMidnight, page:Int): Future[BelgacomListing] = {
    val now = new DateMidnight
    val dayOffset = new Interval(now, date).toPeriod.toStandardDays.getDays
    
    val url = BASE + "/listing"

    //tvmovies[day]:2
	  //tvmovies[genres][]:Oorlog
	  //tvmovies[languages][]:nl
	  //tvmovies[page]:1
    
    val qs = List("tvmovies[day]"-> dayOffset.toString, "tvmovies[page]" -> page.toString)
    WS.url(url).withQueryString(qs:_*).get().map{ response =>
      response.status match {
        case Status.OK =>
          try{
            deserialize[BelgacomListing](response.body)
          }catch{
            case jpe:JsonParseException =>
              throw new RuntimeException(s"Failed to parse $url response to JSON: ${response.body}", jpe)
          }
        case other =>
          throw new RuntimeException(s"Got $other for $url - ${response.body}")
      }
    }
  }

}