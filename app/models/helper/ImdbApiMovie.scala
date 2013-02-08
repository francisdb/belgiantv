package models.helper

import play.api.Logger
import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnoreProperties}

@JsonIgnoreProperties(ignoreUnknown = true)
case class ImdbApiMovie(
	@JsonProperty("imdbID")
	id:String,
	
	@JsonProperty("Title")
	title:String,
	
	@JsonProperty("imdbRating")
	rating:String,
	
	@JsonProperty("imdbVotes")
	votes:String,
	
	@JsonProperty("Runtime")
	runtime:String,
	
	@JsonProperty("Poster")
	poster:String,
	
	@JsonProperty("Genre")
	genre:String,
	
	@JsonProperty("Plot")
	plot:String,
	
	@JsonProperty("Year")
	year:Int,
	
	@JsonProperty("Released")
	released:String,
	
	@JsonProperty("Director")
	director:String
){
	
  def getPercentRating() = {
		if(rating == null){
			-1
		}else{
			try{
				(rating.toDouble * 10).toInt
			} catch {
			  case e:NumberFormatException => 
			    Logger.warn(e.getMessage())
				-1
			}
		}
		
	}
	
	def getDecimalRating() = {
		getPercentRating() / 10
	}
	
	def getUrl() = {
		"http://www.imdb.com/title/" + id
	}
}