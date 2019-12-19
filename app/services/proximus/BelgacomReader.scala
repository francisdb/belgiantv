package services.proximus

import java.time.{LocalDate, ZoneId}
import java.util.Locale

import models.Channel
import models.helper._
import org.slf4j.LoggerFactory
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}
import play.api.libs.json.Json._
import play.api.libs.ws.WSClient
import services.proximus.BelgacomReader._

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object BelgacomProtocol{

  implicit val belgacomProgramFormat = Json.reads[BelgacomProgram]
  implicit val belgacomChannelReads = Json.reads[BelgacomChannel]
  implicit val belgacomChannelListingReads = Json.reads[BelgacomChannelListing]
}

object BelgacomReader{
  final val timeZone = ZoneId.of("Europe/Brussels")

  private final val GraphQLEndpoint = "https://api.proximusmwc.be/graphql"
  private final val log = LoggerFactory.getLogger(classOf[BelgacomReader])
}

class BelgacomReader(ws: WSClient){
  // https://www.proximus.be/pickx/nl/televisie/tv-gids

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
      }

      //filteredChannels.foreach(println)

      searchMovies(date, filteredChannels)
    }

  }

  private def readChannels(): Future[BelgacomChannelListing] = {
    val query = """ query getChannels($language: String!, $queryParams: ChannelQueryParams, $id: String) {
                  |      channels(language: $language, queryParams: $queryParams, id: $id) {
                  |        id
                  |        name
                  |        callLetter
                  |        number
                  |        logo {
                  |          Original
                  |          custom
                  |          __typename
                  |        }
                  |        language
                  |        hd
                  |        replayable
                  |        ottReplayable
                  |        playable
                  |        ottPlayable
                  |        playable
                  |        recordable
                  |        subscribed
                  |        cloudRecordable
                  |        catchUpWindowInHours
                  |        isOttNPVREnabled
                  |        ottNPVRStart
                  |        __typename
                  |      }
                  |    }""".stripMargin
    val variables = obj(
        "language" -> "nl",
        "queryParams" -> obj(),
        "id" -> "0"
      )
    val qs = Seq(
      "query" -> query,
      "operationName" -> "getChannels",
      "variables" -> Json.stringify(variables)
    )

    val req = ws.url(GraphQLEndpoint).withQueryStringParameters(qs:_*)
    req.get().map { response =>
      response.status match {
        case Status.OK =>
          import BelgacomProtocol._
          (response.json \ "data").validate[BelgacomChannelListing] match {
            case JsSuccess(listing, _) => listing
            case JsError(errors) =>
              throw new RuntimeException(s"Failed to parse ${req.uri} response to JSON: $errors ${response.body.take(500)}")
          }
        case otherStatus =>
          throw new RuntimeException(s"Got $otherStatus for $GraphQLEndpoint : ${response.body.take(500)}")
      }
    }
  }
  
  private def searchMovies(date: LocalDate, channels: Seq[BelgacomChannel]): Future[Seq[BelgacomProgramWithChannel]] = {
    //http://www.proximustv.be/zcommon/bep/get-schedules-for-channels?language=nl&externalIds=UID50037%2CUID50084%2CUID50040%2CUID50083%2CUID50066%2CUID50044&unixDate=1486497686

    val url = GraphQLEndpoint

    val query = """query ($language: String!, $startTime: Int!, $endTime: Int!, $options: SchedulesByIntervalOptions) {  schedulesByInterval(language: $language, startTime: $startTime, endTime: $endTime, options: $options) {    trailId    programReferenceNumber    channelId    channel    title    startTime    endTime    timePeriod    image {      Original      custom      __typename    }    imageOnErrorHandler    parentalRating    detailUrl    ottBlackListed    cloudRecordable    grouped    description    shortDescription    category    translatedCategory    categoryId    formattedStartTime    formattedEndTime    subCategory    scheduleTimeFormat    links {      episodeNumber      id      seasonId      seasonName      seriesId      seriesTitle      title      type      __typename    }    seriesId    __typename  }}"""
    val variables = obj(
      "language" -> "nl",
      "options" -> obj(
        "channelIds" -> channels.map(_.id)
      ),
      "startTime" -> date.atStartOfDay(timeZone).toEpochSecond,
      "endTime" -> date.atStartOfDay(timeZone).plusDays(1).toEpochSecond,
    )

    // TODO how do we filter movies?
    // => "category": "C.Movies",

    val qs = List(
      "query" -> query,
      "variables" -> Json.stringify(variables)
    )

    val req = ws.url(url).withQueryStringParameters(qs:_*)
    req.get().map{ response =>
      response.status match {
        case Status.OK =>
          import BelgacomProtocol._
          (response.json \ "data" \ "schedulesByInterval").validate[Seq[BelgacomProgram]] match {
            case JsSuccess(listing, _) =>
              listing.filter(_.category == BelgacomProgram.CATEGORY_MOVIES).map{ program =>
                val channel = channels.find(_.id == program.channelId).get
                val programWithChannel = BelgacomProgramWithChannel(program, channel)
                //println(programWithChannel)
                programWithChannel
              }
            case JsError(errors) =>
              log.error(s"Failed to parse ${req.uri} response to JSON: $errors ${response.body.take(500)}")
              Seq.empty
          }
        case other =>
          log.error(s"Got $other for $url - ${response.body}")
          Seq.empty
      }
    }
  }

}
