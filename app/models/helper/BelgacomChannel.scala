package models.helper

import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnoreProperties}

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