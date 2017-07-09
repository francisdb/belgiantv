package services

import java.time.{LocalDate, ZoneId}
import java.util.Locale

import models.Channel
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WSClient
import models.helper._

import concurrent.Future
import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.http.Status
import BelgacomReader._
import org.slf4j.LoggerFactory

object BelgacomProtocol{

  implicit val belgacomProgramFormat = Json.reads[BelgacomProgram]
  implicit val belgacomChannelReads = Json.reads[BelgacomChannel]
  implicit val belgacomChannelListingReads = Json.reads[BelgacomChannelListing]
}

object BelgacomReader{
  final val timeZone = ZoneId.of("Europe/Brussels")

  private final val BASE = "http://www.proximustv.be/zcommon/bep"
  private final val log = LoggerFactory.getLogger(classOf[BelgacomReader])
}

class BelgacomReader(ws: WSClient){
  // http://www.proximustv.be/nl/televisie/tv-gids

  // api
  //  baseUrl: "http://www.proximustv.be/zcommon/bep",

//  resources: {
//    getUser: "get-user",
//    getChannels: "get-channels",
//    getPlayableChannels: "get-playable-channels",
//    getSchedulesTimelineByScheduleTrailId: "get-schedules-timeline-by-schedule-trail-id",
//    getLiveTimelineForChannels: "get-live-timeline-for-channels",
//    getStreamingUrl: "play",
//    getStreamingMetadata: "metadata",
//    getGlobalData: "get-global-data",
//    startRecording: "start-recording",
//    stopRecording: "stop-recording",
//    deleteRecording: "delete-recording",
//    getRecordings: "get-recordings",
//    refreshCscWatch: "heartbeat",
//    stopCscWatch: "stop",
//    getSchedulesForChannels: "get-schedules-for-channels",
//    getScheduleDetail: "get-schedule-detail"


    
  private val logger = Logger("application.belgacom")
  
  def searchMovies(date: LocalDate): Future[Seq[BelgacomProgramWithChannel]] = {
    readChannels().flatMap{ channels =>

      val lowerFilter = Channel.channelFilter.map(_.toLowerCase(Locale.ENGLISH))
      val filteredChannels = channels.channels.filter{ channel =>
        val unifiedName = models.Channel.unify(channel.name).toLowerCase(Locale.ENGLISH)
        lowerFilter.contains(unifiedName)
      }.to[Seq]

      //filteredChannels.foreach(println)

      searchMovies(date, filteredChannels)
    }

  }

  private def readChannels(): Future[BelgacomChannelListing] = {
    val url = BASE + "/get-channels"
    val qs = Seq(
      "language" -> "nl",
      "userId" -> ""
    )
    ws.url(url).withQueryStringParameters(qs:_*).get().map { response =>
      import BelgacomProtocol._
      response.json.validate[BelgacomChannelListing] match {
        case JsSuccess(listing, _) => listing
        case JsError(errors) =>
          throw new RuntimeException(s"Failed to parse $url response to JSON: $errors ${response.body.take(500)}")
      }
    }
  }
  
  private def searchMovies(date: LocalDate, channels: Seq[BelgacomChannel]): Future[Seq[BelgacomProgramWithChannel]] = {
    //http://www.proximustv.be/zcommon/bep/get-schedules-for-channels?language=nl&externalIds=UID50037%2CUID50084%2CUID50040%2CUID50083%2CUID50066%2CUID50044&unixDate=1486497686

    val url = BASE + "/get-schedules-for-channels"

    // TODO how do we filter movies?
    // => "category": "C.Movies",

//    {
//      "UID50037": [{
//        "trailId": "2017020711357",
//        "programReferenceNumber": "005045664380",
//        "channelId": "UID50037",
//        "channel": null,
//        "title": "Journaallus",
//        "startTime": 1486433700,
//        "endTime": 1486444500,
//        "timePeriod": "03:15 > 06:15",
//        "image": {
//          "Original": "https:\/\/experience-cache.proximustv.be:443\/posterserver\/poster\/EPG\/250_250_B0312EE71AFB8B410C952EAEAC4DBE8E.jpg",
//          "custom": "https:\/\/experience-cache.proximustv.be:443\/posterserver\/poster\/EPG\/%s\/250_250_B0312EE71AFB8B410C952EAEAC4DBE8E.jpg"
//        },
//        "imageOnErrorHandler": null,
//        "parentalRating": "",
//        "detailUrl": "http:\/\/www.proximustv.be\/nl\/televisie\/tv-gids\/epg-2017020711357\/journaallus",
//        "ottBlacklisted": false,
//        "cloudRecordable": true,
//        "grouped": false,
//        "description": "Doorlopende herhalingen in lusvorm tot omstreeks 9. 00 uur.",
//        "shortDescription": "Doorlopende herhalingen in lusvorm tot omstreeks 9. 00 uur.",
//        "category": "C.News",
//        "translatedCategory": "Actualiteit",
//        "categoryId": 0,
//        "formattedStartTime": "03:15",
//        "formattedEndTime": "06:15",
//        "offset": -11.458333333333,
//        "width": 12.5,
//        "isFeaturedEvening": false
//      },

    val qs = List(
      "externalIds" -> channels.map(_.id).mkString(","),
      "language" -> "nl",
      "unixDate" -> date.atStartOfDay(timeZone).toEpochSecond.toString
    )

    qs.foreach(println)

    ws.url(url).withQueryStringParameters(qs:_*).get().map{ response =>
      response.status match {
        case Status.OK =>
          import BelgacomProtocol._
          response.json.validate[Map[String, Seq[BelgacomProgram]]] match {
            case JsSuccess(listing, _) =>
              listing.values.flatten.filter(_.category == BelgacomProgram.CATEGORY_MOVIES).map{ program =>
                val channel = channels.find(_.id == program.channelId).get
                val programWithChannel = BelgacomProgramWithChannel(program, channel)
                println(programWithChannel)
                programWithChannel
              }.to[Seq]
            case JsError(errors) =>
              log.error(s"Failed to parse $url response to JSON: $errors ${response.body.take(500)}")
              Seq.empty
          }
        case other =>
          log.error(s"Got $other for $url - ${response.body}")
          Seq.empty
      }
    }
  }

}