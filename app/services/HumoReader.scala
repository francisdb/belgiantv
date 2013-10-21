package services

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.Scheduler

import org.joda.time.DateMidnight
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

import org.jsoup.Jsoup

import play.api.Logger
import play.api.libs.ws.WS
import play.api.http.Status

import controllers.Application

object HumoReader {
  
  private val logger = Logger("application.humo")
  
  private val dateFormat = DateTimeFormat.forPattern("YYYY-MM-dd")

  private val idBetweenSlashesPattern = """/(\d+)/""".r
  private val yearInBracketsPattern = """\(([0-9][0-9][0-9][0-9])\)""".r

  private val timePattern = """([0-2][0-9]u[0-5][0-9])""".r
  private val timeFormat = DateTimeFormat.forPattern("HH'u'mm")
  
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
    val url = urlForDay(day)
    logger.info("Fetching " + url)
    WS.url(url).get().flatMap{ response =>
      response.status match {
        case Status.OK =>
          Future.successful(parseDay(day, response.body).filter(result => channelFilter.isEmpty || channelFilter.contains(result.channel)))
        case Status.GATEWAY_TIMEOUT =>
          throw GatewayTimeoutException(s"Gateway timeout at remote site when trying to get $url : ${response.body.take(100)}...")
        case other =>
          throw new RuntimeException(s"Got status $other when trying to get $url : ${response.body.take(100)}...")
      }
    }
  }

  case class GatewayTimeoutException(msg: String) extends RuntimeException(msg)
  
  private def urlForDay(day: DateMidnight) = {
    "http://www.humo.be/tv-gids/"+dateFormat.print(day)+"/genres/film"
  }
  
  private def parseDay(day: DateMidnight, body: String): List[HumoEvent] = {
	
    val doc = Jsoup.parse(body)

    //val section = doc.getElementsContainingOwnText("Hoofdzenders").get(0)
    val zenders = doc.getElementsByAttributeValue("class", "articles")
    if(zenders.size == 0){
      throw new RuntimeException(s"No channels found for $day\n${body.take(100)}...")
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
        timePattern findFirstIn meta  match {
          case Some(timeString) =>
            val time = LocalTime.parse(timeString, timeFormat)

            val bd = program.getElementsByAttributeValue("class", "bd").first()
            val info = bd.getElementsByTag("p").first().ownText()
            val url = bd.getElementsByTag("a").attr("href")

            val yearOption = yearInBracketsPattern findFirstMatchIn info map(_.group(1).toInt)
            val idOption = idBetweenSlashesPattern findFirstMatchIn url map(_.group(1))
            val id = idOption.getOrElse(throw new RuntimeException("Could not extract the id"))
            val movie = HumoEvent(id, url, day, channelName, time, movieName, yearOption)
            buf += movie
          case None =>
            logger.warn(s"Could not extract the time from [$meta], skipping [$movieName]")
        }
      }
    }
    buf.distinct.result().toList
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
  
  def toDateTime = {
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