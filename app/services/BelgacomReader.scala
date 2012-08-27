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

object BelgacomReader {
  private val BASE = "http://sphinx.skynet.be/tv-overal/ajax"
  private val LANG_VLAAMS = "lg-fl"
  private val LANG_NEDERLANDS = "lg-nl"

  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def read(date: Date) = {
    val channels = searchLanguage(date, LANG_VLAAMS) ::: searchLanguage(date, LANG_NEDERLANDS)
    channels
  }

  def readMovies(date: Date, dayOffset: Int) = {
    val url = BASE + "/get-movies-data/" + dayOffset + "?_=" + date.getTime()
    Logger.info(url)
    val response = WS.url(url).get.await(30, TimeUnit.SECONDS).get
    val results = deserialize[BelgacomMovieResults](response.body)
    val movies = results.data
    movies.map(m => m.copy(title = StringEscapeUtils.unescapeHtml(m.title)))
  }

  def searchLanguage(date: Date, language: String) = {
    val channels = ListBuffer[BelgacomChannel]()

    var page = 1
    var maxPage = Integer.MAX_VALUE
    while (page <= maxPage) {
      val result = search(date, language, 8, page)
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

  private def search(date: Date, language: String, channelsPerPage: Int, page: Int) = {
    val dayOffset = 0
    val url = BASE + "/get-channel-data/" + channelsPerPage + "/" + page + "/" + language + "/" + dayOffset + "?_=" + date.getTime()
    Logger.info(url)
    val response = WS.url(url).get.await(30, TimeUnit.SECONDS).get
    deserialize[BelgacomResult](response.body)
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