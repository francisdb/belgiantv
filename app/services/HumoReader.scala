package services

import global.Globals
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Scheduler
import org.joda.time.{DateMidnight, DateTime, DateTimeZone, LocalTime}
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import play.api.libs.ws.WS
import play.api.http.Status
import play.api.Play.current
import controllers.Application
import models.Channel

import scala.util.control.NonFatal

object HumoReader {

  final val timezone = DateTimeZone.forID("Europe/Brussels")

  private val logger = Logger("application.humo")
  
  private val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd")

  private val idBetweenSlashesPattern = """/(\d+)/""".r
  private val yearInBracketsPattern = """\(([0-9][0-9][0-9][0-9])\)""".r

  private val timePattern = """([0-2][0-9]u[0-5][0-9])""".r
  private val timeFormat = DateTimeFormat.forPattern("HH'u'mm")


  // channels json
  // http://www.humo.be/api/epg/humosite/channels/main
  // http://www.humo.be/api/epg/humosite/channels/rest

  // epg
  // http://www.humo.be/api/epg/humosite/schedule/main/2015-02-08/full
  // http://www.humo.be/api/epg/humosite/schedule/rest/2015-02-08/full

  
  def fetchDayRetryOnGatewayTimeout(day: DateMidnight, channelFilter: List[String] = List())(implicit scheduler:Scheduler, executionContetext:ExecutionContext):Future[Seq[HumoEvent]] = {
    // TODO better would be to actually use a delay between the retries, we could also use a throttled WS
    val dayData = fetchDay(day, channelFilter)
    dayData.recoverWith{
      case ex:GatewayTimeoutException =>
        logger.warn(s"First humo day fetch failed for $day: ${ex.getMessage}, trying a second time in 1 minute...")
        _root_.akka.pattern.after(1.minute, scheduler)(fetchDay(day, channelFilter))
    }
  }

  def fetchDay(day: DateMidnight, channelFilter: List[String] = List()):Future[Seq[HumoEvent]] = {
    val futures = urlsForDay(day)
      .map( url => fetchPage(url, day, channelFilter))

    Future.sequence(futures).map(_.flatten)
  }

  def fetchPage(url: String, day: DateMidnight, channelFilter: List[String] = List()):Future[Seq[HumoEvent]] = {
    logger.info("Fetching " + url)
    WS.url(url).get().flatMap{ response =>
      response.status match {
        case Status.OK =>
          val dayData = parseDay(day, response.json)
          val interestingData = dayData.filter { result =>
            val unifiedChannel = Channel.unify(result.channel).toLowerCase
            channelFilter.isEmpty || channelFilter.map(_.toLowerCase).contains(unifiedChannel)
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
  
  private def urlsForDay(day: DateMidnight) = {
    val dayFormatted = dateFormat.print(day)
    Seq(
      s"http://www.humo.be/api/epg/humosite/schedule/main/$dayFormatted/full",
      s"http://www.humo.be/api/epg/humosite/schedule/rest/$dayFormatted/full"
    )
  }
  
  private def parseDay(day: DateMidnight, body: JsValue): List[HumoEvent] = {
    import HumoProtocol._
    Json.fromJson[HumoSchedule](body)(scheduleFormat) match {
      case JsSuccess(schedule:HumoSchedule, _) =>
        toEvents(schedule, day)
      case JsError(e) =>
        throw new IllegalStateException(e.toString())
    }

  }

  private def toEvents(humoSchedule: HumoProtocol.HumoSchedule, day: DateMidnight): List[HumoEvent] = {

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
    lazy val startLocalTime = new DateTime(starttime * 1000).withZone(HumoReader.timezone).toLocalTime
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
    day: DateMidnight, 
    channel: String,
    time: LocalTime,
    title: String,
    year: Option[Int]){
  
  def toDateTime = {
    try{
      // artificial check, we say 6:00 starts the day
      if(time.isBefore(LocalTime.parse("6:00"))){
        time.toDateTime(day.plusDays(1).withZoneRetainFields(Globals.timezone))
      }else{
        time.toDateTime(day.withZoneRetainFields(Globals.timezone))
      }
    }catch{
      case NonFatal(ex) =>
        Logger.error(ex.getMessage, ex)
        throw ex
    }
  }
  
  val readable = {
    id + "\t" + day + "\t" + channel + "("+time+")" + "\t" + title
  }
}