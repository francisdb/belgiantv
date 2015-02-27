package models.helper

import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnoreProperties}
import java.util.Date
import org.joda.time.DateTime

case class BelgacomProgram(

  pid: Int,
  title: String,
	lg: String,
	su: Option[String],
	cname: String,
	cnumb: Option[String],
	cid: Int,
	clogo: String,
	sts: Long,
	ets: Long,
  img: String,
	gr: Option[Int],
	grname: Option[String],
	
	bq: Option[String], // ???
	btv: Option[String] // belgacomtv?
	
	//def pk; // duplicate of pkey
	//def seo; // slug
	//def shour; // obsolete start hour:minutes
  
  //public int idx; // visualisation helper index
  //public int lenpx; // visualisation helper length in pixels
  //public int hr; // obsolete start hour
  //public int min; // obsolete start minutes
  //public int ehr; // obsolete end hour
  //public int emin; // obsolete end minutes
  //public String seo; // slug
  //public String hour; // obsolete start hour:minutes
  
  ) {

  val programId = pid
  val language = lg
  val summary = su
  val channelName = cname
  val channelNumber = cnumb
  val channelId = cid
  val channelLogo = clogo
  val startTimeSeconds = sts
  val endTimeSeconds = ets
  val image = img
  val genreId = gr
  val genre = grname

  
  lazy val getProgramUrl = {
    "http://sphinx.skynet.be/tv-overal/tv-gids/programma/" + programId
  }

  lazy val getStart = {
    new Date(startTimeSeconds * 1000)
  }

  lazy val getEnd = {
    new Date(endTimeSeconds * 1000)
  }
  
  lazy val toDateTime = new DateTime(startTimeSeconds * 1000)

  override def toString = title + " " + channelName + "@" + toDateTime
  
}