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

    val section = doc.getElementsContainingOwnText("Hoofdzenders").get(0)
    val zenders = doc.getElementsByAttributeValue("class", "broadcaster")

    val buf = new ArrayBuffer[HumoEvent] 

    for (zender <- zenders) {
      val channelName = zender.getElementsByTag("img").first().attr("title")
      val programs = zender.getElementsByAttributeValue("class", "programme")
      for (program <- programs) {
        val movieName = program.getElementsByAttributeValue("class", "title").first().text()
        val time = LocalTime.parse(program.getElementsByAttributeValue("class", "starttime").first().text(), timeFormat)
        val yearSpan = Option(program.getElementsByAttributeValue("class", "year"))
        // this check for empty is actually strage because the list should be empty if none found
        // and seems to contain an empty element here
        val year = if(yearSpan.size > 0 && !yearSpan.head.text().isEmpty()) Option(yearSpan.head.text().toInt) else None        
        val id = program.attr("id").replaceAll("event_", "")
        val url = program.getElementsByTag("a").first().attr("href")
        val movie = HumoEvent(id, url, day, channelName, time, movieName, year)
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