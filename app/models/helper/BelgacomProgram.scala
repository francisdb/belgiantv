package models.helper

import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnoreProperties}
import java.util.Date
import org.joda.time.DateTime

@JsonIgnoreProperties(ignoreUnknown = true)
case class BelgacomProgram(

  @JsonProperty("pid") programId: String,

  title: String,

	@JsonProperty("lg")
	language:String,

	@JsonProperty("su")
	summary:String,
	
	@JsonProperty("cname")
	channelName:String,
	
	@JsonProperty("cnumb")
	channelNumber:String,
	
	@JsonProperty("cid")
	channelId:Int,
	
	@JsonProperty("clogo")
	channelLogo:String,
	
	@JsonProperty("sts")
	startTimeSeconds:Long,
	
	@JsonProperty("ets")
	endTimeSeconds:Long,

	@JsonProperty("img")
	image:String,
	
	@JsonProperty("gr")
	genreId:Int,
	
	@JsonProperty("grname")
	genre:String,
	
	bq:String, // ???
	btv:String // belgacomtv?
	
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
  
  def getProgramUrl() = {
    "http://sphinx.skynet.be/tv-overal/tv-gids/programma/" + programId
  }

  def getStart() = {
    new Date(startTimeSeconds * 1000)
  }

  def getEnd() = {
    new Date(endTimeSeconds * 1000)
  }
  
  def toDateTime = new DateTime(startTimeSeconds * 1000)

  override def toString = title + " " + channelName + "@" + toDateTime
  
}