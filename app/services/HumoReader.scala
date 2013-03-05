package services
import org.joda.time.DateMidnight
import org.jsoup.nodes.Document
import scala.collection.JavaConversions._
import scala.collection.mutable.LinkedList
import org.jsoup.Jsoup
import scala.collection.mutable.ArrayBuffer
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import org.joda.time.DateTimeZone
import controllers.Application
import play.api.libs.ws.WS

import scala.concurrent.ExecutionContext.Implicits.global

object HumoReader {
  
  private val logger = Logger("application.humo")
  
  private val dateFormat = DateTimeFormat.forPattern("YYYY-MM-dd")
  private val timeFormat = DateTimeFormat.forPattern("HH'u'mm")
  
  def fetchDay(day: DateMidnight, channelFilter: List[String] = List()) = {
    val url = urlForDay(day)
    logger.info("Fetching " + url)
    WS.url(url).get().map { response =>
      parseDay(day, response.body).filter(result => channelFilter.isEmpty || channelFilter.contains(result.channel))
    }
  }
  
  private def urlForDay(day: DateMidnight) = {
    "http://www.humo.be/tv-gids/"+dateFormat.print(day)+"/genres/film"
  }
  
  private def parseDay(day: DateMidnight, body: String): List[HumoEvent] = {
	
    val doc = Jsoup.parse(body)

    //val section = doc.getElementsContainingOwnText("Hoofdzenders").get(0)
    val zenders = doc.getElementsByAttributeValue("class", "articles")
    if(zenders.size == 0){
      throw new RuntimeException("No channels found")
    }

    val buf = new ArrayBuffer[HumoEvent] 

    for (zender <- zenders) {
      val channelName = zender.getElementsByTag("h3").first().ownText()
      val programs = zender.getElementsByAttributeValue("class", "content")
      if(zenders.size == 0){
        throw new RuntimeException("No programs for " + channelName)
      }
      for (program <- programs) {
        val movieName = program.getElementsByTag("a").first().text()
        val meta = program.getElementsByAttributeValue("class", "meta").first().ownText()
        val timeString = meta.substring(meta.indexOf('|') + 2)
        val time = LocalTime.parse(timeString, timeFormat)

        val bd = program.getElementsByAttributeValue("class", "bd").first()
        val info = bd.getElementsByTag("p").first().ownText()

        // TODO sould be possible to simplify this part using a more functional way
        val yearInBracketsPattern = """\((\d+)\)""".r
        val yearFound = yearInBracketsPattern.findAllIn(info).matchData
        val yearOption = if (yearFound.hasNext){
          Some(yearFound.next().group(1).toInt)
        }else{
          None
        }

        val url = bd.getElementsByTag("a").attr("href")

        // TODO sould be possible to simplify this part using a more functional way
        val idBetweenSlashesPattern = """/(\d+)/""".r
        val idFound = idBetweenSlashesPattern.findAllIn(url).matchData
        val idOption = if (idFound.hasNext){
          Some(idFound.next().group(1))
        }else{
          None
        }
        val id = idOption.getOrElse(throw new RuntimeException("Could not extract the id"))

        val movie = HumoEvent(id, url, day, channelName, time, movieName, yearOption)
        buf += movie
      }
    }
    buf.distinct.result.toList
  }

}

case class HumoEvent(
    id:String,
    url:String,
    day: DateMidnight, 
    channel: String,
    time: LocalTime,
    title: String,
    year: Option[Int]){
  
  def toDateTime() = {
    try{
      // artificial check, we say 6:00 starts the day
      if(time.isBefore(LocalTime.parse("6:00"))){
        time.toDateTime(day.plusDays(1).withZoneRetainFields(Application.timezone))
      }else{
        time.toDateTime(day.withZoneRetainFields(Application.timezone))
      }
    }catch{
      case ex: Exception => Logger.error(ex.getMessage, ex)
        throw ex
    }
  }
  
  val readable = {
    id + "\t" + day + "\t" + channel + "("+time+")" + "\t" + title
  }
}