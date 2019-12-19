package services.proximus

import java.time.Instant

case class BelgacomProgramWithChannel(
  program: BelgacomProgram,
  channel: BelgacomChannel
){
  override def toString = program.toString
}

object BelgacomProgram{
  final val CATEGORY_MOVIES = "C.Movies"
}

case class BelgacomProgram(
  trailId: String,
  programReferenceNumber: String,
  channelId: String,
  title: String,

  startTime: Int,
  endTime: Int,
  description: String,
  shortDescription: Option[String],
  category: String,
  detailUrl: Option[String]){


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


  lazy val getStart = {
    Instant.ofEpochSecond(startTime)
  }

  lazy val getEnd = {
    Instant.ofEpochSecond(endTime)
  }
  
  lazy val toDateTime = Instant.ofEpochSecond(startTime)

  override def toString = s"$title on $channelId at ${getStart.atZone(BelgacomReader.timeZone)} -> ${getEnd.atZone(BelgacomReader.timeZone)}"
  
}
