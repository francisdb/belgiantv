package services

import org.joda.time.DateMidnight
import org.joda.time.format.DateTimeFormat
import org.joda.time.Interval
import org.joda.time.LocalTime
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import play.api.Logger
import org.jsoup.Jsoup
import scala.collection.JavaConversions._
import play.api.libs.ws.WS


object YeloReader {
  
  private val logger = Logger("application.yelo")
  
  private val dateFormat = DateTimeFormat.forPattern("dd-MM-YYYY")
  
  def fetchDay(day: DateMidnight, channelFilter: List[String] = List()) = {
    val urls = urlsForDay(day)
    logger.info("Fetching " + urls)
    val first = WS.url(urls(0)).get().map { response =>
      parseDay(day, response.json).filter(result => channelFilter.isEmpty || channelFilter.map(_.toLowerCase).contains(yeloChannelToHumoChannel(result.channel).toLowerCase))
    }
    val second = WS.url(urls(1)).get().map { response =>
      parseDay(day, response.json).filter(result => channelFilter.isEmpty || channelFilter.map(_.toLowerCase).contains(yeloChannelToHumoChannel(result.channel).toLowerCase))
    }
    for( f <- first; s <- second ) yield ( f ++ s )
  }
  
  private def urlsForDay(day: DateMidnight) = {
    List(
      "http://yelo.be/detail/tvgids?date="+dateFormat.print(day),
      "http://yelo.be/detail/tvgids?date="+dateFormat.print(day) + "&group=anderstalig"
    )
  }
  
  private def parseDay(day: DateMidnight, body: JsValue) = {

    val channels = (body \ "Result" \ "Channels").as[String]
    val channelsDoc = Jsoup.parse(channels)
    
    val imgs = channelsDoc.getElementsByTag("img")
    val imglist = imgs.subList(0, imgs.size).toList
    val channelMap = imglist.map{ img =>
      val alt = img.attr("alt")
      val src = img.attr("src")
      val file = src.substring(src.lastIndexOf("/")+1)
      val code = file.substring(0,5)
      (code, alt)
    }.toMap
    
    
    val events = (body \ "Result" \ "Events").as[String]
    val doc = Jsoup.parse(events)
    
    val pvrbroadcasts = (body \ "Result" \ "Meta" \ "pvrbroadcasts").as[JsObject]
    
    pvrbroadcasts.fields.map{ field =>
      
      val eventDiv = doc.getElementsByAttributeValue("id", "js-event-" + field._1).first()
      val url = "http://yelo.be" + eventDiv.getElementsByTag("a").attr("href")
      
      val interval = new Interval((field._2 \ "start_time").as[String].toLong * 1000, (field._2 \ "end_time").as[String].toLong * 1000)
      
      val channelWebpvrIid = (field._2 \ "channel_webpvr_id").as[String]
      val channel = channelMap.get(channelWebpvrIid).getOrElse("UNKNOWN")
      
      YeloEvent(field._1.toInt, (field._2 \ "title").toString(), url, channel, interval)
    }

  }
  
  case class YeloEvent (
      id:Int, 
      name:String, 
      url: String, 
      channel: String,
      interval: Interval){
    
    def toDateTime() = interval.getStart
  }
      
  def yeloChannelToHumoChannel(channel:String) = {
    channel.replace("Ned1", "Ned 1").replace("Ned2", "Ned 2").replace("Ned3", "Ned 3")
  }
    
}