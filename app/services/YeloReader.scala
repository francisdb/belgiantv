package services

import org.joda.time.DateMidnight
import org.joda.time.format.DateTimeFormat
import org.joda.time.Interval
import org.joda.time.LocalTime
import play.api.libs.json.{JsArray, JsValue, JsObject}
import play.api.Logger
import org.jsoup.Jsoup
import scala.collection.JavaConversions._
import play.api.libs.ws.WS
import java.text.ParseException
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import models.Channel
import com.sun.xml.internal.fastinfoset.tools.FI_SAX_Or_XML_SAX_SAXEvent
import concurrent.{Future, Await}
import org.codehaus.jackson.JsonParseException
import play.api.http.Status


object YeloReader {
  
  private val logger = Logger("application.yelo")

  def fetchDay(day: DateMidnight, channelFilter: List[String] = List()) = {
    val vlaams = fetchDayData(day)
    val anderstalig = fetchDayData(day, Some("anderstalig"))
    val filter = createFilter(channelFilter)
    for(
      v <- vlaams;
      a <- anderstalig
    ) yield ( v.filter(filter) ++ a.filter(filter) )
  }


  private def fetchDayData(day: DateMidnight, groupOption: Option[String] = None): Future[Seq[YeloReader.YeloEvent]] = {
    val dateFormat = DateTimeFormat.forPattern("dd-MM-YYYY")
    val baseUrl = "http://yelotv.be/tvgids/detail?date=" + dateFormat.print(day)
    //val group = groupOption.getOrElse("alles")
    val group = groupOption.getOrElse("vlaams")
    val url = baseUrl + "&group=" + group
    logger.info("Fetching " + url)
    WS.url(url)
      .withHeaders(
        "Referer" -> "http://yelotv.be/tvgids",
        "X-Requested-With" -> "XMLHttpRequest"
      ).get().map { response =>
      if(response.status != Status.OK){
        throw new RuntimeException(s"Got ${response.status} while fetching $url -> ${response.body}")
      }
      try{
        parseDay(url, response.json)
      }catch{
        case ex:JsonParseException => throw new RuntimeException("Failed to parse reponse for " + url + " to json: " + response.body.take(100) + "...", ex)
      }
    }
  }

  def createFilter(channelFilter: List[String]): (YeloReader.YeloEvent) => Boolean = { result =>
    channelFilter.isEmpty || channelFilter.map(_.toLowerCase).contains(Channel.unify(result.channel).toLowerCase)
  }

  private def parseDay(url: String, body: JsValue): Seq[YeloEvent] = {
    val success = (body \ "Success").as[Boolean]
    if(!success){
      throw new RuntimeException(s"Response unsuccessful while fetching $url -> $body")
    }

    val channelMap = parseChannels(body)

    val events = (body \ "Result" \ "Events").as[String]
    val doc = Jsoup.parse(events)

    val jsonNode = body \ "Result" \ "Meta" \ "pvrbroadcasts"

    if(jsonNode.isInstanceOf[JsObject]){
      val pvrbroadcasts = jsonNode.as[JsObject]
      pvrbroadcasts.fields.map {
        case (eventIdentification, eventObject) =>
          val eventId = eventIdentification.toInt
          val title = (eventObject \ "title").as[String]
          val channelWebpvrId = (eventObject \ "channel_webpvr_id").as[String]
          val startTime = (eventObject \ "start_time").as[String].toLong * 1000
          val endTime = (eventObject \ "end_time").as[String].toLong * 1000

          val eventDiv = Option(doc.getElementsByAttributeValue("id", "js-event-" + eventId).first())
          eventDiv.map {
            div =>
              val url = "http://yelotv.be" + div.getElementsByTag("a").attr("href")
              val channel = channelMap.get(channelWebpvrId).getOrElse("UNKNOWN")
              YeloEvent(eventId, title, url, channel, new Interval(startTime, endTime))
          }.getOrElse {
            logger.warn("No div found for %s %s at %s".format(eventId, title, new DateTime(startTime)))
            YeloEvent(eventId, title, null, "UNKNOWN", new Interval(startTime, endTime))
          }
      }
    }else{
      logger.warn(s"No yelo movies found for $url")
      Seq.empty
    }
  }

  private def parseChannels(body: JsValue) = {
    val channels = (body \ "Result" \ "Channels").as[String]
    val channelsDoc = Jsoup.parse(channels)

    val imgs = channelsDoc.getElementsByTag("img")
    val imglist = imgs.subList(0, imgs.size).toList
    val channelMap = imglist.map {
      img =>
        val alt = img.attr("alt")
        val src = img.attr("src")
        val file = src.substring(src.lastIndexOf("/") + 1)
        val code = file.substring(0, 5)
        (code, alt)
    }.toMap
    channelMap
  }

  def toHDUrl(url: String): String = {
    url.replace("vtm", "vtm-hd")
      .replace("canvas", "canvas-hd")
      .replace("een", "een-hd")
      .replace("vier", "vier-hd")
      .replace("vijf", "vijf-hd")
      .replace("2be", "2be-hd")
  }

  case class YeloEvent (
      id:Int, 
      name:String, 
      url: String, 
      channel: String,
      interval: Interval){
    
    def toDateTime() = interval.getStart
  }
}