package services

import java.util.Date
import java.util.ArrayList
import models.helper.BelgacomChannel
import play.api.Logger
import org.codehaus.jackson.map.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.codehaus.jackson.`type`.TypeReference
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
import play.api.libs.concurrent.Promise$
import play.api.libs.concurrent.Promise
import models.helper.BelgacomListing
import models.helper.BelgacomListing


object BelgacomReader {
  private val BASE = "http://sphinx.skynet.be/tv-overal/ajax"
    
  private val logger = Logger("application.belgacom")

  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def readMovies(date: DateMidnight) = {
    val now = new DateMidnight
    val dayOffset = new Interval(now, date).toPeriod().toStandardDays().getDays()
    searchMovies(date)
  }

  def belgacomChannelToHumoChannel(channel:String) = {
    channel.replace("Vijf TV", "VijfTV")
      .replace("Nederland 2", "Ned 2").replace("Nederland 1", "Ned 1")
      .replace("Nederland 3", "Ned 3")
      .replace("BBC One London", "BBC 1").replace("BBC Two London", "BBC 2")
  }
  
  def searchMovies(date: DateMidnight): Promise[List[BelgacomProgram]] = {
    val page = 1
    searchMovies(date, page).map{ result =>
      result.tvmovies.data.map(program => program.copy(title = StringEscapeUtils.unescapeHtml(program.title)))
    }
  }
  
  private def searchMovies(date: DateMidnight, page:Int): Promise[BelgacomListing] = {
    val now = new DateMidnight
    val dayOffset = new Interval(now, date).toPeriod().toStandardDays().getDays()
    
    val url = BASE + "/listing"

    //tvmovies[day]:2
	//tvmovies[genres][]:Oorlog
	//tvmovies[languages][]:nl
	//tvmovies[page]:1
    
    val params = Map(
        "tvmovies[day]"->Seq(dayOffset.toString),
        "tvmovies[page]"->Seq(page.toString))
    
    WS.url(url).post(params).map{ response =>
      //println(response.body)
      deserialize[BelgacomListing](response.body)
    }
    
  }

  private def deserialize[T: Manifest](value: String) : T =
    mapper.readValue(value, new TypeReference[T]() {
      override def getType = new ParameterizedType {
        val getActualTypeArguments = manifest[T].typeArguments.map(_.erasure.asInstanceOf[Type]).toArray
        val getRawType = manifest[T].erasure
        val getOwnerType = null
      }
  })
}