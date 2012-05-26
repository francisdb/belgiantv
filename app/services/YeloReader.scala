package services
import org.joda.time.DateMidnight
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import org.jsoup.Jsoup
import org.joda.time.Interval
import org.joda.time.LocalTime

object YeloReader2 {
  
  val dateFormat = DateTimeFormat.forPattern("dd-MM-YYYY")
  //val timeFormat = DateTimeFormat.forPattern("HH'u'mm")
  
  
  def urlForDay(day: DateMidnight) = {
    "http://yelo.be/detail/tvgids?date="+dateFormat.print(day)
  }
  
  def parseDay(day: DateMidnight, body: JsValue) = {

    val events = (body \ "Result" \ "Events").as[String]
    val doc = Jsoup.parse(events)
    
    val pvrbroadcasts = (body \ "Result" \ "Meta" \ "pvrbroadcasts").as[JsObject]
    
    pvrbroadcasts.fields.map{ field =>
      
      val eventDiv = doc.getElementsByAttributeValue("id", "js-event-" + field._1).first()
      val link = "http://yelo.be" + eventDiv.getElementsByTag("a").attr("href")
      
      val interval = new Interval((field._2 \ "start_time").as[String].toLong * 1000, (field._2 \ "end_time").as[String].toLong * 1000)
      
      YeloEvent(field._1.toInt, (field._2 \ "title").toString(), link, interval)
    }

  }
  
  case class YeloEvent (
      id:Int, 
      name:String, 
      link: String, 
      interval: Interval)
    
}