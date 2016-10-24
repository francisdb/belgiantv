package services

import org.joda.time.DateMidnight
import org.joda.time.format.DateTimeFormat
import org.joda.time.Interval
import play.api.libs.json.{Json, JsValue, JsObject}
import play.api.Logger
import play.api.Play.current
import org.jsoup.Jsoup
import scala.collection.JavaConversions._
import play.api.libs.ws.WS
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import models.Channel
import concurrent.Future
import com.fasterxml.jackson.core.JsonParseException
import play.api.http.Status


object YeloReader {
  
  private val logger = Logger("application.yelo")

  def fetchDay(day: DateMidnight, channelFilter: List[String] = List.empty): Future[Seq[YeloEvent]] = {
    val vlaams = fetchDayData(day, "vlaams")
    val hd = fetchDayData(day, "hd")
    val anderstalig = fetchDayData(day, "anderstalig")
    val filter = createFilter(channelFilter)
    for(
      v <- vlaams;
      h <- hd;
      a <- anderstalig
    ) yield{
//      println((v ++ h ++ a).map(_.channel).toSet)
//      println((v ++ h ++ a).filter(filter).map(_.channel).toSet)
      (v ++ h ++ a).filter(filter)
    }
  }


  private def fetchDayData(day: DateMidnight, group: String = "alles"): Future[Seq[YeloReader.YeloEvent]] = {
    val dateFormat = DateTimeFormat.forPattern("dd-MM-YYYY")
    val baseUrl = "http://yelotv.be/tvgids/detail?date=" + dateFormat.print(day)
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
    val unifiedChannel = Channel.unify(result.channel).toLowerCase
    channelFilter.isEmpty || channelFilter.map(_.toLowerCase).contains(unifiedChannel)
  }

  private def parseDay(url: String, body: JsValue): Seq[YeloEvent] = {
    val success = (body \ "Success").as[Boolean]
    if(!success){
      throw new RuntimeException(s"Response unsuccessful while fetching $url -> $body")
    }

    val channelMap = parseChannels(body)

    val events = (body \ "Result" \ "Events").as[String]
    val doc = Jsoup.parse(events)

    val jsonNode = (body \ "Result" \ "Meta" \ "pvrbroadcasts")
      // see https://github.com/playframework/playframework/issues/4851
      .toOption.getOrElse(throw new RuntimeException(s"pvrbroadcasts not found for $url"))

    jsonNode match{
      case pvrbroadcasts: JsObject =>
        pvrbroadcasts.fields.map { case (eventIdentification, eventObject) =>
          val eventId = eventIdentification.toInt
          val title = (eventObject \ "title").as[String]
          val startTimeMillis = (eventObject \ "start_time").as[Long] * 1000
          val endTimeMillis = (eventObject \ "end_time").as[Long] * 1000

          val divId = "js-event-" + eventId
          val eventDiv = Option(doc.getElementsByAttributeValue("id", divId).first())
          eventDiv.map { div =>
            val li = div.parent()
            val epgchanneleventsindex = li.attr("epgchanneleventsindex").toLong
            val url = "http://yelotv.be" + div.getElementsByTag("a").attr("href")

            val channel = channelMap.getOrElse(epgchanneleventsindex, "UNKNOWN")
            YeloEvent(eventId, title, Some(url), channel, new Interval(startTimeMillis, endTimeMillis))
          }.getOrElse {
            logger.warn("No div found for %s %s at %s".format(divId, title, new DateTime(startTimeMillis)))
            YeloEvent(eventId, title, None, "UNKNOWN", new Interval(startTimeMillis, endTimeMillis))
          }
        }
      case other =>
        logger.warn(s"No yelo movies found for $url")
        Seq.empty
    }
  }

  private def parseChannels(body: JsValue):Map[Long, String] = {
    val channels = (body \ "Result" \ "Channels").as[String]
    val channelsDoc = Jsoup.parse(channels)
    val lis = channelsDoc.getElementsByTag("li")
    val liList = lis.subList(0, lis.size).toList
    val channelMap = liList.map { li =>
      val epgchannelindex = li.attr("epgchannelindex").toLong
      val imgOption = Option(li.getElementsByTag("img").first())
      val alt = imgOption.map{ img =>
        img.attr("alt")
      }.getOrElse("UNKNOWN")

      //val src = img.attr("src")
      //val file = src.substring(src.lastIndexOf("/") + 1)
      //val code = file.substring(0, 5)
      (epgchannelindex, alt)
    }.toMap
    channelMap
  }

  case class YeloEvent (
      id: Int,
      name: String,
      url: Option[String],
      channel: String,
      interval: Interval){
    
    lazy val startDateTime = interval.getStart
  }
}