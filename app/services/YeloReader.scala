package services

import java.util.Locale

import org.joda.time.DateMidnight
import org.joda.time.format.DateTimeFormat
import org.joda.time.Interval
import play.api.libs.json._
import play.api.Logger
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import models.Channel

import concurrent.Future
import play.api.http.Status
import services.YeloReader.{ScheduleDay, YeloEvent}
import YeloReader._

object YeloReader{

  val pubbaBase = "https://www.yeloplay.be/api/pubba"

  val headers = Seq(
    "Referer" -> "https://www.yeloplay.be/tv-gids"
  )

  case class Channels(
    channels: Seq[Channel],
    json_create_date: String // "2016-11-17T23:06:45+01:00"
  )

  case class Channel(
    id: Long,             // 207
    pvrid: String,        // "MM0BE"
    url: String,          // "vtm-hd"
    name: String,         // "VTM HD"
    priority: Long,       // 1
    stbuniquename: String // "vtmhd"
  )

  case class ScheduleDay(
    epg: Epg,
    json_create_date: String, // "2016-11-17T22:29:06+01:00"
    schedule: Seq[Schedule]
  )

  case class Schedule(
    eventpvrid: String, // "MM24E",
    channelid: Long, // 198
    name: String, // "één HD",
    broadcast: Seq[Broadcast]
  )

  // we had to remove some fields as we can only go up to ~22 fields
  case class Broadcast(
    blackout: Option[String], // null
    content: Option[String], // "3.1.1.1"
    contentlabel: Option[String], // "dagelijks nieuws"
//    crid: String, //"crid://magnetmedia.be/14025651_18599981"
//    dubbedlng: Option[String], // null
    endtime: Long, // 1479456000
//    epgbacklandscape: String, // "https://cache.ps.yelo.prd.telenet-ops.be/yposter/images/epgbacklandscape/51082683.jpg"
//    epgbackportrait: String, // ""
    eventid: Long, // 39932155
    eventpvrid: String, // "MM24E2FC9"
//    format: Option[String], // null
//    formatlabel: Option[String], // null
//    image: String, // "https://cache.ps.yelo.prd.telenet-ops.be/yposter/images/MMImages/63478050.jpg"
//    imiid: String, // "imi:00100000009637DA"
//    ishd: Boolean, // true
//    islive: Boolean, // false
    language: Option[String], // null
//    location: String, // ""
    mapurl: String, // "/tv/journaallus-nl/een-hd/18-november-2016/02-25"
    poster: String, // "https://cache.ps.yelo.prd.telenet-ops.be/yposter/images/MMImages/63478050.jpg"
//    recendtime: Option[String], // null
//    recstarttime: Option[String], // null
//    seasoncrid: String, // "crid://magnetmedia.be/14025651"
//    serieid: Long, // 696007
//    seriemmcode: String, // "8342089Z"
//    seriescrid: String, // "crid://magnetmedia.be/show/8342089"
    shortdescription: Option[String], // null
    standardid: Long, // 950699
    starttime: Long, // 1479432300
    subtitlelng: String, // ""
    title: String, // "Journaallus"
    url: String // "/programma/journaallus/seizoen_9999/aflevering_9999/het-journaal"
  )

  case class Epg(
    date: String, // "2016-11-18"
    datemin: String, //"2008-06-11",
    datemax: String, //"2016-12-02",
    language: String // "nl"
  )

  case class YeloEvent (
    id: Long,
    name: String,
    url: Option[String],
    channel: String,
    interval: Interval){

    lazy val startDateTime = interval.getStart
  }

  implicit val channelFormat = Json.format[Channel]
  implicit val channelsFormat = Json.format[Channels]

  implicit val broadcastFormat = Json.format[Broadcast]
  implicit val scheduleFormat = Json.format[Schedule]
  implicit val epgFormat = Json.format[Epg]
  implicit val scheduleDayForamt = Json.format[ScheduleDay]
}

//
// GET /api/pubba/v1/events/schedule-day/outformat/json/lng/nl/channel/8/channel/534/channel/585/channel/625/channel/801/day/2016-11-18/platform/web/ HTTP/1.1
// Host: www.yeloplay.be
// Connection: keep-alive
// Pragma: no-cache
// Cache-Control: no-cache
// Accept: application/json, text/plain, */*
// User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36
// Referer: https://www.yeloplay.be/tv-gids
// Accept-Encoding: gzip, deflate, sdch, br
// Accept-Language: en-US,en;q=0.8,nl;q=0.6,fr;q=0.4
// Cookie: x-telenet-yelo-session-cookie=state%3DAUTHCODE%3Abef93892-a50d-44a4-b5a1-42ceb338b4d5; language=nl; telenet_device_id=43f2aa93-2121-4878-a6ad-da355a6bf127; web_generated_id=f8f570d0-f8e4-4ca9-9e7e-9622bc4ad547
//
// https://www.yeloplay.be/api/pubba/v1/events/schedule-day/outformat/json/lng/nl/channel/8/channel/534/channel/585/channel/625/channel/801/day/2016-11-17/platform/web/

class YeloReader(ws: WSClient) {
  
  private val logger = Logger("application.yelo")

  def fetchDay(day: DateMidnight, channelFilter: List[String] = List.empty): Future[Seq[YeloEvent]] = {
    fetchChannels().flatMap{ channels =>
      logger.info("selected channels: " + channels.map(_.name).mkString(", "))
      fetchDayData(day, channels)
    }
  }

  private def fetchChannels() = {
    val url = s"$pubbaBase/v3/channels/all/outformat/json/platform/web/"
    ws.url(url).withHttpHeaders(headers:_*).get().map { response =>
      if (response.status != Status.OK) {
        throw new RuntimeException(s"Got ${response.status} while fetching $url -> ${response.body}")
      }
      response.json.validate[Channels] match {
        case e:JsError =>
          val errors = JsError.toFlatForm(e).mkString(", ")
          throw new RuntimeException(s"Failed to parse reponse for $url to json: ${response.body.take(100)}... $errors"  )
        case JsSuccess(channels,_) =>
          logger.debug("all channels: " + channels.channels.map(_.name).mkString(", "))
          val lowerFilter = Channel.channelFilter.map(_.toLowerCase(Locale.ENGLISH))
          channels.channels.filter{ channel =>
            val unifiedName = models.Channel.unify(channel.name).toLowerCase(Locale.ENGLISH)
            lowerFilter.contains(unifiedName)
          }
      }
    }
  }

  private def fetchDayData(day: DateMidnight, channels: Seq[Channel]): Future[Seq[YeloReader.YeloEvent]] = {
    val dateFormat = DateTimeFormat.forPattern("YYYY-MM-dd")
    val baseUrl = s"$pubbaBase/v1/events/schedule-day/outformat/json/lng/en/"
    val orderedChannelIds = channels.map(_.id).sorted

    val channelsPart = orderedChannelIds.map(c => s"channel/$c/").mkString("")
    val dayPart = s"day/${dateFormat.print(day)}/"
    val platformPart = "platform/web/"
    val url = baseUrl + channelsPart + dayPart + platformPart
    logger.info(s"Fetching $url")

    ws.url(url).withHeaders(headers:_*).get().map { response =>
      if(response.status != Status.OK){
        throw new RuntimeException(s"Got ${response.status} while fetching $url -> ${response.body}")
      }
      response.json.validate[ScheduleDay] match {
        case e:JsError =>
          val errors = JsError.toFlatForm(e).mkString(", ")
          throw new RuntimeException(s"Failed to parse reponse for $url to json: ${response.body.take(100)}... $errors"  )
        case JsSuccess(daySchedule,_) =>
          parseDay(daySchedule, channels)
      }
    }
  }

  def createFilter(channelFilter: List[String]): (YeloReader.YeloEvent) => Boolean = { result =>
    val unifiedChannel = Channel.unify(result.channel).toLowerCase
    channelFilter.isEmpty || channelFilter.map(_.toLowerCase).contains(unifiedChannel)
  }

  private def parseDay(daySchedule: ScheduleDay, channels: Seq[Channel]): Seq[YeloEvent] = {
    daySchedule.schedule.flatMap{ schedule =>
      val channel = channels
        .find(_.id == schedule.channelid)
        .map(_.name)
        .getOrElse("UNKNOWN")
      schedule.broadcast.map{ broadcast =>
        val interval = new Interval(broadcast.starttime * 1000, broadcast.endtime * 1000)
        val url = "https://www.yeloplay.be" + broadcast.mapurl
        YeloEvent(
          broadcast.eventid,
          broadcast.title,
          Some(url),
          channel,
          interval
        )
      }
    }
  }

}