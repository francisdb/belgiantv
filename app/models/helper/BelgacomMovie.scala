package models.helper

import java.util.Date
import org.codehaus.jackson.annotate.JsonProperty
import org.codehaus.jackson.annotate.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
case class BelgacomMovie(
    
	@JsonProperty("pid")
	programId:Long,
	
	@JsonProperty("pkey")
	programKey:String,
	
	@JsonProperty("ti")
	title:String,
	
	@JsonProperty("lg")
	language:String,

	@JsonProperty("su")
	summary:String,
	
	@JsonProperty("cname")
	channelname:String,
	
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
) {
	def getProgramUrl() = {
		"http://sphinx.skynet.be/tv-overal/tv-gids/programma/" + programKey
	}
	
	def getStart() = {
		new Date(startTimeSeconds * 1000)
	}
	
	def getEnd() = {
		new Date(endTimeSeconds * 1000)
	}
}