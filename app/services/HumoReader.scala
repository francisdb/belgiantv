package services

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalTime}

import global.Globals
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Scheduler
import play.api.Logger
import play.api.http.Status
import models.Channel
import play.api.libs.ws.WSClient

import scala.util.control.NonFatal

import HumoReader._

object HumoReader {
  val logger = Logger("application.humo")
}

class HumoReader(ws: WSClient) {


  
  private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  private val idBetweenSlashesPattern = """/(\d+)/""".r
  private val yearInBracketsPattern = """\(([0-9][0-9][0-9][0-9])\)""".r

  private val timePattern = """([0-2][0-9]u[0-5][0-9])""".r
  private val timeFormat = DateTimeFormatter.ofPattern("HH'u'mm")


  // channels json
  // https://www.humo.be/api/epg/humosite/channels/main
  // https://www.humo.be/api/epg/humosite/channels/rest

  // epg
  // https://www.humo.be/api/epg/humosite/schedule/main/2015-02-08/full
  // https://www.humo.be/api/epg/humosite/schedule/rest/2015-02-08/full

  
  def fetchDayRetryOnGatewayTimeout(day: LocalDate, channelFilter: List[String] = List())(implicit scheduler:Scheduler):Future[Seq[HumoEvent]] = {
    // TODO better would be to actually use a delay between the retries, we could also use a throttled WS
    val dayData = fetchDay(day, channelFilter)
    dayData.recoverWith{
      case ex:GatewayTimeoutException =>
        logger.warn(s"First humo day fetch failed for $day: ${ex.getMessage}, trying a second time in 1 minute...")
        _root_.akka.pattern.after(1.minute, scheduler)(fetchDay(day, channelFilter))
    }
  }

  def fetchDay(day: LocalDate, channelFilter: List[String] = List()):Future[Seq[HumoEvent]] = {
    val futures = urlsForDay(day)
      .map( url => fetchPage(url, day, channelFilter))

    Future.sequence(futures).map(_.flatten)
  }

  def fetchPage(url: String, day: LocalDate, channelFilter: List[String] = List()):Future[Seq[HumoEvent]] = {
    logger.info("Fetching " + url)
    ws.url(url).get().flatMap{ response =>
      response.status match {
        case Status.OK =>
          val dayData = parseDay(day, response.json).map{ e =>
            e.copy(channel = Channel.unify(e.channel))
          }
          val interestingData = dayData.filter { result =>
            channelFilter.isEmpty || channelFilter.map(_.toLowerCase).contains(result.channel.toLowerCase)
          }
          Future.successful(interestingData)
        case Status.GATEWAY_TIMEOUT =>
          throw GatewayTimeoutException(s"Gateway timeout at remote site when trying to get $url : ${response.body.take(100)}...")
        case other =>
          throw new RuntimeException(s"Got status $other when trying to get $url : ${response.body.take(100)}...")
      }
    }
  }

  case class GatewayTimeoutException(msg: String) extends RuntimeException(msg)
  
  private def urlsForDay(day: LocalDate) = {
    val dayFormatted = dateFormat.format(day)
    Seq(
      s"https://www.humo.be/api/epg/humosite/schedule/main/$dayFormatted/full",
      s"https://www.humo.be/api/epg/humosite/schedule/rest/$dayFormatted/full"
    )
  }
  
  private def parseDay(day: LocalDate, body: JsValue): List[HumoEvent] = {
    import HumoProtocol._
    Json.fromJson[HumoSchedule](body)(scheduleFormat) match {
      case JsSuccess(schedule:HumoSchedule, _) =>
        toEvents(schedule, day)
      case JsError(e) =>
        throw new IllegalStateException(e.toString())
    }

  }

  private def toEvents(humoSchedule: HumoProtocol.HumoSchedule, day: LocalDate): List[HumoEvent] = {

    humoSchedule.broadcasters.flatMap{ broadcaster =>
      broadcaster.events.filter(_.isMovie) map { event =>
        HumoEvent(
          event.id.toString,
          event.url,
          day,
          broadcaster.display_name,
          event.startLocalTime,
          event.program.title.getOrElse(""),
          event.program.yearInt)
      }
    }.toList
  }

}


object HumoProtocol {

  case class HumoSchedule(date: String, broadcasters: Seq[HumoBroadcaster])

  // also contains a media section
  case class HumoBroadcaster(id: Long, code: String, display_name: String, events: Seq[HumoEvent])

  case class HumoEvent(id: Long, url: String, event_id: String, starttime: Long, endtime: Long, labels: Option[Seq[String]], program: HumoProgram) {
    val isMovie = labels.exists(_.contains("film"))
    lazy val startLocalTime = Instant.ofEpochSecond(starttime).atZone(Globals.timezone).toLocalTime
  }

  case class HumoProgram(
                          id: Long,
                          title: Option[String],
                          description: Option[String],
                          content_short: Option[String],
                          content_long: Option[String],
                          year: Option[String],
                          genres: Seq[String]
                          ) {
    val yearInt = year.map(_.toInt)
  }

  implicit val programFormat = Json.format[HumoProgram]
  implicit val eventFormat = Json.format[HumoEvent]
  implicit val broadcasterFormat = Json.format[HumoBroadcaster]
  implicit val scheduleFormat = Json.format[HumoSchedule]

}



case class HumoEvent(
    id:String,
    url:String,
    day: LocalDate,
    channel: String,
    time: LocalTime,
    title: String,
    year: Option[Int]){
  
  def toDateTime: Instant = {
    // artificial check, we say 6:00 starts the day
    val dayStart = LocalTime.of(6,0)
    try{
      if(time.isBefore(LocalTime.of(6,0))){
        time.atDate(day.plusDays(1)).atZone(Globals.timezone).toInstant
      }else{
        time.atDate(day).atZone(Globals.timezone).toInstant
      }
    }catch{
      case NonFatal(ex) =>
        logger.error(ex.getMessage, ex)
        throw ex
    }
  }
  
  val readable = {
    id + "\t" + day + "\t" + channel + "("+time+")" + "\t" + title
  }
}
