package services

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate}
import java.util.UUID

import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Scheduler
import play.api.Logger
import play.api.http.Status
import models.Channel
import play.api.libs.ws.WSClient
import HumoReader._
import HumoProtocol._
import util.FutureExt

object HumoReader {
  val logger = Logger("application.humo")
}

class HumoReader(ws: WSClient) {

  //see also https://github.com/tvgrabbers/sourcematching/blob/master/sources/source-humo.be.json

  private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def fetchDayRetryOnGatewayTimeout(day: LocalDate, channelFilter: Set[String] = Set.empty)(
    implicit scheduler: Scheduler
  ): Future[Seq[HumoEvent]] = {
    // TODO better would be to actually use a delay between the retries, we could also use a throttled WS
    val dayData = fetchDay(day, channelFilter)
    dayData.recoverWith {
      case ex: GatewayTimeoutException =>
        logger.warn(s"First humo day fetch failed for $day: ${ex.getMessage}, trying a second time in 1 minute...")
        _root_.akka.pattern.after(1.minute, scheduler)(fetchDay(day, channelFilter))
    }
  }

  def fetchDay(day: LocalDate, channelFilter: Set[String] = Set.empty): Future[Seq[HumoEvent]] =
    fetchPage(day, channelFilter)

  def fetchPage(day: LocalDate, channelFilter: Set[String] = Set.empty): Future[Seq[HumoEvent]] = {
    val url = apiDayUrl(day)
    logger.info(s"Fetching $url")
    ws.url(url).get().flatMap { response =>
      response.status match {
        case Status.OK =>
          parseDay(day, response.json).map { results =>
            val dayData = results.map { e =>
              e.copy(channel = Channel.unify(e.channel))
            }
            dayData.filter { result =>
              channelFilter.isEmpty || channelFilter.map(_.toLowerCase).contains(result.channel.toLowerCase)
            }
          }
        case Status.GATEWAY_TIMEOUT =>
          throw GatewayTimeoutException(
            s"Gateway timeout at remote site when trying to get $url : ${response.body.take(100)}..."
          )
        case other =>
          throw new RuntimeException(s"Got status $other when trying to get $url : ${response.body.take(100)}...")
      }
    }
  }

  def fetchDetail(playableType: String, broadcastId: UUID): Future[MovieDetail] = {
    val url = apiDetailUrl(playableType, broadcastId)
    logger.info("Fetching " + url)
    ws.url(url).get().map { response =>
      response.status match {
        case Status.OK =>
          response.json.validate[MovieDetail](movieDetailFormat) match {
            case JsSuccess(detail, _) => detail
            case JsError(e)           => throw new IllegalStateException(e.toString())
          }
        case Status.GATEWAY_TIMEOUT =>
          throw GatewayTimeoutException(
            s"Gateway timeout at remote site when trying to get $url : ${response.body.take(100)}..."
          )
        case other =>
          throw new RuntimeException(s"Got status $other when trying to get $url : ${response.body.take(100)}...")
      }
    }
  }

  case class GatewayTimeoutException(msg: String) extends RuntimeException(msg)

  private def apiDayUrl(day: LocalDate): String = {
    val dayFormatted = dateFormat.format(day)
    s"https://www.humo.be/tv-gids/api/v2/broadcasts/$dayFormatted"
  }

  private def apiDetailUrl(playableType: String, broadcastId: UUID): String = {
    val singularPlayableType = if (playableType.endsWith("s")) {
      playableType.dropRight(1)
    } else {
      playableType
    }
    s"https://www.humo.be/tv-gids/api/v2/detail/$singularPlayableType/$broadcastId"
  }

  private def websiteMovieUrl(channelSeoKey: String, broadcastUUID: UUID) =
    s"https://www.humo.be/tv-gids/$channelSeoKey/uitzending/film/$broadcastUUID"

  private def parseDay(day: LocalDate, body: JsValue): Future[Seq[HumoEvent]] = {
    import HumoProtocol._
    Json.fromJson[Listing](body)(listingFormat) match {
      case JsSuccess(listing: Listing, _) =>
        toEvents(listing, day)
      case JsError(e) =>
        Future.failed(new IllegalStateException(e.toString()))
    }
  }

  private def toEvents(listing: Listing, day: LocalDate): Future[Seq[HumoEvent]] = {
    //println(listing.channels.map(_.name))
    val movieBroadcasts = listing.channels.flatMap { channel =>
      channel.broadcasts.filter(_.isMovie).map(b => (channel, b))
    }
    // TODO we might want to limit concurrency here
    FutureExt.traverseWithBoundedParallelism(2)(movieBroadcasts) {
      case (channel, broadcast) =>
        fetchDetail(broadcast.playableType, broadcast.uuid).map { detail =>
          // https://www.humo.be/tv-gids/q2/uitzending/film/e50d9707-7c3c-45e2-a301-6e6ee3ac89ab
          // https://www.humo.be/tv-gids/een/uitzending/aflevering/a39b43aa-68db-4c34-8b56-ca1dc031ea05
          val url = websiteMovieUrl(channel.seoKey, broadcast.uuid)
          HumoEvent(
            broadcast.uuid.toString,
            url,
            Instant.ofEpochMilli(broadcast.from),
            channel.name,
            broadcast.title,
            detail.productionYear
          )
        }
    }

  }
}

object HumoProtocol {

  case class Listing(
    from: Long,      // epoch milli
    to: Long,        // epoch milli
    timestamp: Long, // request epoch milli?
    channels: Seq[ChannelListing]
  )

  case class ChannelListing(
    name: String,   // "Een"
    seoKey: String, // "een"
    uuid: UUID,
    channelLogoUrl: String,
    broadcasts: Seq[Broadcast],
    liveStreamLinks: Seq[LiveStreamLink],
    order: Int
  )

  case class Broadcast(
    uuid: UUID,
    playableUuid: UUID,
    programUuid: Option[UUID],
    channelUuid: UUID,
    from: Long,    // epoch milli
    to: Long,      // epoch milli
    title: String, //"Winterbeelden"
    live: Boolean,
    rerun: Boolean,
    legalIcons: Seq[String], // [ PGAL ]
    duration: Option[Int],   // seconds?
    synopsis: Option[String],
    imageUrl: Option[String],
    imageFormat: Option[String], // WALLPAPER / LANDSCAPE
    playableType: String,        // episodes / movies / oneoffs
    genre: Option[String],       // Magazine / Sport / Radioprogramma / Informatief Programma
    subGenres: Seq[String],      // [ Homeshoppingprogramma ]
    tip: Boolean,
    rating: Option[Int], // 0-100?
    videoOnDemandLinks: Seq[JsValue],
    fromIso: String, //"2020-02-05T08:00:00"
    toIso: String    //"2020-02-05T09:00:00"
  ) {
    def isMovie: Boolean = playableType == "movies"
  }

  case class LiveStreamLink(
    url: String,   // "https://www.vrt.be/vrtnu/livestream/"
    title: String, // "Kijk op VRT.Nu"
    iconImageUrl: String
  )

//  {
//    "uuid": "2d671c70-c3bb-471f-9f9b-8791e051186d",
//    "channelUuid": "8c1d440a-8b88-4ef8-9026-2acd542d7ceb",
//    "channelName": "Caz",
//    "channelLogoUrl": "https://images3.persgroep.net/rcs/QNT5mDOt1YHhXZFlA6CvZ7RSPvI/diocontent/148415383/_fitwidth/500?appId=da11c75db9b73ea0f41f0cd0da631c71",
//    "channelSeoKey": "caz",
//    "playableUuid": "6c0f24d0-f6d2-40d0-862c-59dfd8296a50",
//    "from": 1580910600000,
//    "to": 1580916300000,
//    "title": "The Marksman",
//    "live": false,
//    "rerun": true,
//    "imageUrl": "https://images2.persgroep.net/rcs/eb1iwQCpyUDo67y6ex6zCX6mw70/diocontent/145277986/_fill/1408/792?appId=da11c75db9b73ea0f41f0cd0da631c71",
//    "imageFormat": "WALLPAPER",
//    "legalIcons": [
//    "PGAL"
//    ],
//    "duration": 5700,
//    "productionYear": 2005,
//    "synopsisXS": "Tsjetsjeense rebellen zijn een Russische kerncentrale binnengedrongen en hebben de controle over de reactors overgenomen.",
//    "synopsisS": "Tsjetsjeense rebellen zijn een Russische kerncentrale binnengedrongen en hebben de controle over de reactors overgenomen. Een groep soldaten van de Amerikaanse Special Forces onder leiding van de ervaren Painter krijgt de opdracht de nucleaire installatie te heroveren en de gijzelaars te bevrijden. Maar al snel groeit bij Painter het vermoeden dat hij en zijn team in de val gelokt zijn.",
//    "synopsisM": "Tsjetsjeense rebellen zijn een Russische kerncentrale binnengedrongen en hebben de controle over de reactors overgenomen. Een groep soldaten van de Amerikaanse Special Forces onder leiding van de ervaren Painter krijgt de opdracht de nucleaire installatie te heroveren en de gijzelaars te bevrijden. Maar al snel groeit bij Painter het vermoeden dat hij en zijn team in de val gelokt zijn.",
//    "synopsisOriginal": "Tsjetsjeense rebellen zijn een Russische kerncentrale binnengedrongen en hebben de controle over de reactors overgenomen. Een groepje soldaten van de US Special Forces, onder leiding van de ervaren Painter, krijgt de opdracht de nucleaire installatie te heroveren en de gijzelaars te bevrijden. Maar al snel groeit bij Painter het vermoeden dat hij en zijn team in de val gelokt zijn.",
//    "description": "Amerikaanse actiethriller van Marcus Adams uit 2005.",
//    "playableType": "movies",
//    "genre": "Film",
//    "subGenres": [
//    "Tv-thriller"
//    ],
//    "collaborators": [
//    {
//      "function": "Acteurs",
//      "persons": [
//      {
//        "name": "Anthony Warren",
//        "characters": [
//        "Naish"
//        ],
//        "order": 1
//      },
//      {
//        "name": "William Hope",
//        "characters": [
//        "Johnathan Tensor"
//        ],
//        "order": 2
//      },
//      {
//        "name": "Wesley Snipes",
//        "characters": [
//        "Painter"
//        ],
//        "order": 3
//      },
//      {
//        "name": "Emma Samms",
//        "characters": [
//        "Amanda Jacks"
//        ],
//        "order": 4
//      },
//      {
//        "name": "Peter Youngblood Hills",
//        "characters": [
//        "Hargreaves"
//        ],
//        "order": 5
//      }
//      ]
//    },
//    {
//      "function": "Regisseurs",
//      "persons": [
//      {
//        "name": "Marcus Adams",
//        "characters": null,
//        "order": 1
//      }
//      ]
//    }
//    ],
//    "ratings": [],
//    "videoOnDemandLinks": [
//    {
//      "url": "https://vtm.be/vtmgo/the-marksman~m6c0f24d0-f6d2-40d0-862c-59dfd8296a50?app-context=vtmgo%3A%2F%2Fmovie%3Fid%3D6c0f24d0-f6d2-40d0-862c-59dfd8296a50",
//      "title": "Kijk op VTM GO",
//      "iconImageUrl": "https://images3.persgroep.net/rcs/NfNaogTFRuNGrlz994CNSrvm5oA/diocontent/157152201/_fitheight/150?appId=da11c75db9b73ea0f41f0cd0da631c71"
//    }
//    ],
//    "descriptors": [],
//    "fromIso": "2020-02-05T13:50:00",
//    "toIso": "2020-02-05T15:25:00"
//  }

  case class MovieDetail(
    productionYear: Option[Int] // 2005
  )

  implicit val broadcastFormat      = Json.format[Broadcast]
  implicit val liveStreamLinkFormat = Json.format[LiveStreamLink]
  implicit val channelListingFormat = Json.format[ChannelListing]
  implicit val listingFormat        = Json.format[Listing]
  implicit val movieDetailFormat    = Json.format[MovieDetail]
}

case class HumoEvent(
  id: String,
  url: String,
  from: Instant,
  channel: String,
  title: String,
  year: Option[Int]
) {

  def toDateTime: Instant = from

  val readable = {
    id + "\t" + from + "\t" + channel + "\t" + title
  }
}
