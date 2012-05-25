package services
import org.joda.time.DateMidnight
import org.jsoup.nodes.Document
import scala.collection.JavaConversions._
import scala.collection.mutable.LinkedList
import org.jsoup.Jsoup
import scala.collection.mutable.ArrayBuffer
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

object HumoReader {

  val timeFormat = DateTimeFormat.forPattern("HH'u'mm")
  
  def parseDay(day: DateMidnight, body: String): List[DayChannelMovie] = {

    val doc = Jsoup.parse(body)

    val section = doc.getElementsContainingOwnText("Hoofdzenders").get(0)
    val zenders = doc.getElementsByAttributeValue("class", "broadcaster")

    val buf = new ArrayBuffer[DayChannelMovie] 

    for (zender <- zenders) {
      val channelName = zender.getElementsByTag("img").first().attr("title")
      val programs = zender.getElementsByAttributeValue("class", "programme")
      //println(channelName)
      for (program <- programs) {
        val movieName = program.getElementsByAttributeValue("class", "title").first().text()
        val time = LocalTime.parse(program.getElementsByAttributeValue("class", "starttime").first().text(), timeFormat)
        
        //println("\t" + movieName)
        val movie = DayChannelMovie(day, channelName, time, movieName)
        buf += movie
      }
    }
    
    buf.distinct.result.toList
  }

}

case class DayChannelMovie(
    day: DateMidnight, 
    channel: String,
    time: LocalTime,
    movie: String){
  
  val readable = {
    day + "\t" + channel + "("+time+")" + "\t" + movie
  }
}