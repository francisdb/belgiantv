package services

import java.util.Date
import java.util.ArrayList
import models.helper.BelgacomChannel
import play.api.Logger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.core.JsonParseException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import models.helper.BelgacomResult
import play.api.libs.ws.WS
import org.apache.commons.lang.StringEscapeUtils
import java.util.concurrent.TimeUnit
import scala.collection.mutable.ListBuffer
import org.joda.time.DateMidnight
import org.joda.time.Interval
import models.helper.BelgacomListing
import models.helper.BelgacomProgram
import play.api.libs.concurrent.Promise
import models.helper.BelgacomListing
import models.helper.BelgacomListing
import concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global


object BelgacomReader extends JacksonMapper{
  private val BASE = "http://sphinx.skynet.be/tv-overal/ajax"
    
  private val logger = Logger("application.belgacom")

  def readMovies(date: DateMidnight) = {
    val now = new DateMidnight
    val dayOffset = new Interval(now, date).toPeriod().toStandardDays().getDays()
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
    val dayOffset = new Interval(now, date).toPeriod().toStandardDays().getDays()
    
    val url = BASE + "/listing"

    //tvmovies[day]:2
	//tvmovies[genres][]:Oorlog
	//tvmovies[languages][]:nl
	//tvmovies[page]:1
    
    val qs = List(("tvmovies[day]"-> dayOffset.toString), ("tvmovies[page]" -> page.toString))
    WS.url(url).withQueryString(qs:_*).get().map{ response =>
      //println(response.body)
      deserialize[BelgacomListing](response.body)
    }
    
  }

}