package models.helper

import org.codehaus.jackson.annotate.JsonProperty
import org.codehaus.jackson.annotate.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
case class BelgacomChannel(
	@JsonProperty("cid")
	channelId:String,
	
	@JsonProperty("cname")
	channelName:String,
	
	@JsonProperty("cnum")
	channelNumber:String,
	
	@JsonProperty("clogo")
	channelLogo:String,
	
	pr:List[BelgacomProgram]    
) {

  override def toString() = {
    channelName + " [" + pr.size + "]"
  }
}