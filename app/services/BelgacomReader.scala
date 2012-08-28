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
import models.helper.BelgacomMovie
import java.util.concurrent.TimeUnit
import scala.collection.mutable.ListBuffer
import models.helper.BelgacomMovieResults
import org.joda.time.DateMidnight
import org.joda.time.Interval

object BelgacomReader {
  private val BASE = "http://sphinx.skynet.be/tv-overal/ajax"
  private val LANG_VLAAMS = "lg-fl"
  private val LANG_NEDERLANDS = "lg-nl"
    
  private val logger = Logger("application.belgacom")

  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def read(date: DateMidnight) = {
    val channels = searchLanguage(date, LANG_VLAAMS) ::: searchLanguage(date, LANG_NEDERLANDS)
    channels
  }

  def readMovies(date: DateMidnight) = {
    val now = new DateMidnight
    val dayOffset = new Interval(now, date).toPeriod().toStandardDays().getDays()
    val url = BASE + "/get-movies-data/" + dayOffset// + "?_=" + date.getMillis()
    logger.info(url)
    WS.url(url).get.map { response =>
      val results = deserialize[BelgacomMovieResults](response.body)
      if(results.maxPage != 0){
    	throw new RuntimeException("paged results for movies not supported")
      }
      val movies = results.data
      movies.map(m => m.copy(title = StringEscapeUtils.unescapeHtml(m.title)))
    }
  }

  def searchLanguage(date: DateMidnight, language: String) = {
    val channels = ListBuffer[BelgacomChannel]()

    var page = 1
    var maxPage = Integer.MAX_VALUE
    while (page <= maxPage) {
      // TODO find a way to properly handle this async
      val result = search(date, language, 8, page).await(30, TimeUnit.SECONDS).get
      for (channel <- result.data) {
        if (channel.channelId != null && !channel.channelId.isEmpty()) {
          // fix html in titles
          val c = channel.copy(pr = channel.pr.map(program => program.copy(title = StringEscapeUtils.unescapeHtml(program.title))))
          channels += c
        }
      }
      maxPage = result.maxPage
      page = page + 1
    }
    channels.toList
  }

  def belgacomChannelToHumoChannel(channel:String) = {
    channel.replace("Vijf TV", "VijfTV")
      .replace("Nederland 2", "Ned 2").replace("Nederland 1", "Ned 1")
      .replace("Nederland 3", "Ned 3")
      .replace("BBC One London", "BBC 1").replace("BBC Two London", "BBC 2")
  }
  
  private def search(date: DateMidnight, language: String, channelsPerPage: Int, page: Int) = {
    val dayOffset = 0
    val url = BASE + "/get-channel-data/" + channelsPerPage + "/" + page + "/" + language + "/" + dayOffset + "?_=" + date.getMillis()
    logger.info(url)
    WS.url(url).get.map{ response =>
       deserialize[BelgacomResult](response.body)
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